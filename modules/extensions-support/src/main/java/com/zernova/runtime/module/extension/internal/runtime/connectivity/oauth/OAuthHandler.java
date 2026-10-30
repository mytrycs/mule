/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth;

import static com.zernova.runtime.api.store.ObjectStoreManager.BASE_PERSISTENT_OBJECT_STORE_KEY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_STORE_MANAGER;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;

import static java.util.stream.Collectors.toMap;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.el.ZernovaExpressionLanguage;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.lock.LockFactory;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.runtime.api.store.ObjectStoreSettings;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.oauth.api.OAuthService;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;

/**
 * Contains base logic for extensions which require OAuth authentication. It tracks and handles the obtained tokens and provides
 * base functionality.
 * <p>
 * One concrete implementation of this class must exist per each supported grant type.
 *
 * @param <Dancer> The generic type of the OAuth dancer to use
 */
public abstract class OAuthHandler<Dancer> implements Lifecycle {

  private static final Logger LOGGER = getLogger(OAuthHandler.class);

  @Inject
  protected ZernovaContext muleContext;

  @Inject
  protected LockFactory lockFactory;

  @Inject
  @Named(OBJECT_STORE_MANAGER)
  protected ObjectStoreManager objectStoreManager;

  @Inject
  protected ZernovaExpressionLanguage expressionEvaluator;

  @Inject
  protected ArtifactEncoding artifactEncoding;

  // TODO: MULE-10837 this should be a plain old @Inject
  protected LazyValue<OAuthService> oauthService;

  protected Function<OAuthConfig, ObjectStore> objectStoreLocator;


  protected final Map<String, Dancer> dancers = new ConcurrentHashMap<>();
  protected boolean started = false;

  protected Map<String, String> getParameterExtractors(OAuthConfig config) {
    return getParameterExtractors(config.getParameterExtractors());
  }

  protected Map<String, String> getParameterExtractors(Map<Field, String> extractors) {
    return extractors.entrySet().stream()
        .collect(toMap(entry -> entry.getKey().getName(), Entry::getValue));
  }

  @Override
  public void initialise() throws InitialisationException {
    oauthService = new LazyLookup<>(OAuthService.class, muleContext);
    objectStoreLocator = buildObjectStoreLocator();
  }

  @Override
  public void start() throws ZernovaException {
    for (Dancer dancer : dancers.values()) {
      start(dancer);
    }
    started = true;
  }

  protected void start(Dancer dancer) throws ZernovaException {
    initialiseIfNeeded(dancer, muleContext);
    startIfNeeded(dancer);
  }

  @Override
  public void stop() throws ZernovaException {
    dancers.forEach((key, dancer) -> {
      try {
        disable(key, dancer);
      } catch (Exception e) {
        LOGGER.warn("Found exception while trying to stop OAuth dancer for config " + key, e);
      }
    });
    dancers.clear();
  }

  @Override
  public void dispose() {
    // no default action
  }

  protected Function<OAuthConfig, ObjectStore> buildObjectStoreLocator() {
    return config -> {
      Optional<OAuthObjectStoreConfig> storeConfig = config.getStoreConfig();
      String storeName = storeConfig
          .map(OAuthObjectStoreConfig::getObjectStoreName)
          .orElse(BASE_PERSISTENT_OBJECT_STORE_KEY);
      return objectStoreManager.getOrCreateObjectStore(storeName, ObjectStoreSettings.builder().persistent(true).build());
    };
  }

  private void disable(String ownerConfigName, Dancer dancer) {
    try {
      stopIfNeeded(dancer);
    } catch (Exception e) {
      LOGGER.warn("Found exception trying to Stop OAuth dancer for config " + ownerConfigName, e);
    } finally {
      disposeIfNeeded(dancer, LOGGER);
    }
  }

  public Function<OAuthConfig, ObjectStore> getObjectStoreLocator() {
    return objectStoreLocator;
  }

}
