/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.connection;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.DISABLE_JMX_FOR_COMMONS_POOL2;
import static com.zernova.runtime.api.config.PoolingProfile.INITIALISE_ALL;
import static com.zernova.runtime.api.config.PoolingProfile.INITIALISE_NONE;
import static com.zernova.runtime.api.config.PoolingProfile.INITIALISE_ONE;
import static com.zernova.runtime.api.config.PoolingProfile.WHEN_EXHAUSTED_GROW;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.internal.connection.ConnectionUtils.logPoolStatus;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.min;
import static java.time.Duration.ofMillis;
import static java.util.Objects.requireNonNull;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionHandler;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.PoolingListener;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.ZernovaException;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ConnectionManagementStrategy} which returns connections obtained from a {@link #pool}
 *
 * @param <C> the generic type of the connections to be managed
 * @since 4.0
 */
final class PoolingConnectionManagementStrategy<C> extends ConnectionManagementStrategy<C> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PoolingConnectionManagementStrategy.class);

  private final PoolingProfile poolingProfile;
  private final GenericObjectPool<C> pool;
  private final String poolId;
  private final PoolingListener<C> poolingListener;

  /**
   * Creates a new instance
   *
   * @param connectionProvider     the {@link ConnectionProvider} used to manage the connections
   * @param poolingProfile         the {@link PoolingProfile} which configures the {@link #pool}
   * @param poolingListener        a {@link PoolingListener}
   * @param featureFlaggingService the {@link FeatureFlaggingService}
   */
  PoolingConnectionManagementStrategy(ConnectionProvider<C> connectionProvider, PoolingProfile poolingProfile,
                                      PoolingListener<C> poolingListener, String ownerConfigName,
                                      FeatureFlaggingService featureFlaggingService) {
    super(connectionProvider);
    this.poolingProfile = poolingProfile;
    this.poolingListener = requireNonNull(poolingListener);
    this.poolId = ownerConfigName.concat("-").concat(generateId());
    this.pool = createPool(ownerConfigName, featureFlaggingService);
  }

  /**
   * Returns a {@link ConnectionHandler} which wraps a connection obtained from the {@link #pool}
   *
   * @return a {@link ConnectionHandler}
   * @throws ConnectionException if the connection could not be obtained
   */
  @Override
  public ConnectionHandler<C> getConnectionHandler() throws ConnectionException {
    try {
      return new PoolingConnectionHandler<>(borrowConnection(), pool, poolId, poolingListener, connectionProvider);
    } catch (ConnectionException e) {
      throw e;
    } catch (NoSuchElementException e) {
      throw new ConnectionException("Connection pool is exhausted", e);
    } catch (Exception e) {
      throw new ConnectionException("An exception was found trying to obtain a connection: " + e.getMessage(), e);
    }
  }

  private C borrowConnection() throws Exception {
    C connection = pool.borrowObject();
    LOGGER.debug("Acquiring connection {} from the pool {}", connection.toString(), poolId);
    logPoolStatus(LOGGER, pool, poolId);
    try {
      poolingListener.onBorrow(connection);
    } catch (Exception e) {
      pool.invalidateObject(connection);
      throw e;
    }

    return connection;
  }

  /**
   * Closes the pool, causing the contained connections to be closed as well.
   *
   * @throws ZernovaException
   */
  // TODO: MULE-9082 - pool.close() doesn't destroy unreturned connections
  @Override
  public void close() throws ZernovaException {
    try {
      logPoolStatus(LOGGER, pool, poolId);
      LOGGER.debug("Closing pool {}", poolId);
      pool.close();
    } catch (Exception e) {
      throw new DefaultZernovaException(createStaticMessage("Could not close connection pool"), e);
    }
  }

  private GenericObjectPool<C> createPool(String ownerConfigName,
                                          FeatureFlaggingService featureFlaggingService) {
    GenericObjectPoolConfig<C> config = new GenericObjectPoolConfig<>();

    config.setMaxIdle(poolingProfile.getMaxIdle());
    if (featureFlaggingService != null && featureFlaggingService.isEnabled(DISABLE_JMX_FOR_COMMONS_POOL2)) {
      config.setJmxEnabled(false);
    }

    switch (poolingProfile.getExhaustedAction()) {
      case WHEN_EXHAUSTED_GROW:
        config.setMaxTotal(MAX_VALUE);
        config.setBlockWhenExhausted(false);
        break;
      case PoolingProfile.WHEN_EXHAUSTED_FAIL:
        config.setMaxTotal(poolingProfile.getMaxActive());
        config.setBlockWhenExhausted(false);
        break;
      case PoolingProfile.WHEN_EXHAUSTED_WAIT:
        config.setMaxTotal(poolingProfile.getMaxActive());
        config.setBlockWhenExhausted(true);
        break;
    }


    config.setMaxWait(ofMillis(poolingProfile.getMaxWait()));
    config.setMinEvictableIdleDuration(ofMillis(poolingProfile.getMinEvictionMillis()));
    config.setTimeBetweenEvictionRuns(ofMillis(poolingProfile.getEvictionCheckIntervalMillis()));
    GenericObjectPool<C> genericPool = new GenericObjectPool<>(new ObjectFactoryAdapter(), config);
    LOGGER.debug("Creating pool with ID {} for config {}", poolId, ownerConfigName);

    applyInitialisationPolicy(genericPool);
    logPoolStatus(LOGGER, genericPool, poolId);

    return genericPool;
  }

  protected void applyInitialisationPolicy(GenericObjectPool<C> pool) {
    int initialConnections;
    switch (poolingProfile.getInitialisationPolicy()) {
      case INITIALISE_NONE:
        initialConnections = 0;
        break;
      case INITIALISE_ONE:
        initialConnections = 1;
        break;
      case INITIALISE_ALL:
        if (poolingProfile.getMaxActive() < 0) {
          initialConnections = poolingProfile.getMaxIdle();
        } else if (poolingProfile.getMaxIdle() < 0) {
          initialConnections = poolingProfile.getMaxActive();
        } else {
          initialConnections = min(poolingProfile.getMaxActive(), poolingProfile.getMaxIdle());
        }
        break;
      default:
        throw new IllegalStateException("Unexpected value for pooling profile initialization policy: "
            + poolingProfile.getInitialisationPolicy());
    }

    LOGGER.debug("Initializing pool {} with {} initial connections", poolId, initialConnections);
    for (int t = 0; t < initialConnections; t++) {
      try {
        pool.addObject();
      } catch (Exception e) {
        LOGGER.warn("Failed to create a connection while applying the pool initialization policy.", e);
      }
    }
  }

  public PoolingProfile getPoolingProfile() {
    return poolingProfile;
  }

  private class ObjectFactoryAdapter extends BasePooledObjectFactory<C> {

    @Override
    public PooledObject<C> wrap(C obj) {
      return new DefaultPooledObject<>(obj);
    }

    @Override
    public C create() throws Exception {
      C connection = connectionProvider.connect();
      LOGGER.debug("Created connection {}", connection.toString());
      return connection;
    }

    @Override
    public void destroyObject(final PooledObject<C> connection) throws Exception {
      LOGGER.debug("Disconnecting connection {}", connection.getObject().toString());
      connectionProvider.disconnect(connection.getObject());
    }

    @Override
    public boolean validateObject(final PooledObject<C> obj) {
      return false;
    }

  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }

}
