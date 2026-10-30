/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.GRACEFUL_SHUTDOWN_DEFAULT_TIMEOUT;

import static java.lang.Long.parseLong;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;

import com.zernova.runtime.api.serialization.ObjectSerializer;
import com.zernova.runtime.api.time.TimeSupplier;
import com.zernova.runtime.config.internal.bean.DefaultObjectSerializerDelegate;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.config.ConfigurationExtension;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.config.DynamicConfigExpiration;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.internal.config.ExpressionCorrelationIdGenerator;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;

import java.util.List;
import java.util.OptionalLong;

import org.springframework.beans.factory.SmartFactoryBean;

import jakarta.inject.Inject;

/**
 * This class is a "SmartFactoryBean" which allows a few XML attributes to be set on the otherwise read-only ZernovaConfiguration. It
 * looks up the ZernovaConfiguration from the ZernovaContext and does some class-casting to be able to modify it. Note that this will
 * only work if the ZernovaContext has not yet been started, otherwise the modifications will be ignored (and warnings logged).
 */
// TODO MULE-9638 remove usage of SmartFactoryBean
public class ZernovaConfigurationConfigurator extends AbstractComponentFactory<ZernovaConfiguration>
    implements SmartFactoryBean<ZernovaConfiguration> {

  private ZernovaContext muleContext;

  @Inject
  private DefaultObjectSerializerDelegate objectSerializerDelegate;

  @Inject
  private TimeSupplier timeSupplier;

  // We instantiate DefaultZernovaConfiguration to make sure we get the default values for
  // any properties not set by the user.
  private final DefaultZernovaConfiguration config = new DefaultZernovaConfiguration();

  private boolean explicitDynamicConfigExpiration;

  private OptionalLong shutdownTimeout = OptionalLong.empty();

  @Override
  public boolean isEagerInit() {
    return true;
  }

  @Override
  public boolean isPrototype() {
    return false;
  }

  private void applyDefaultIfNoObjectSerializerSet(DefaultZernovaConfiguration configuration) {
    ObjectSerializer configuredSerializer = config.getDefaultObjectSerializer();

    if (configuredSerializer != null) {
      configuration.setDefaultObjectSerializer(configuredSerializer);
      objectSerializerDelegate.setDelegate(configuredSerializer);
      if (muleContext instanceof DefaultZernovaContext) {
        ((DefaultZernovaContext) muleContext).setObjectSerializer(configuredSerializer);
      }
    }
  }

  @Override
  public Class<?> getObjectType() {
    return ZernovaConfiguration.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void setDefaultResponseTimeout(int responseTimeout) {
    config.setDefaultResponseTimeout(responseTimeout);
  }

  public void setDefaultTransactionTimeout(int defaultTransactionTimeout) {
    config.setDefaultTransactionTimeout(defaultTransactionTimeout);
  }

  public void setShutdownTimeout(long shutdownTimeout) {
    this.shutdownTimeout = OptionalLong.of(shutdownTimeout);
  }

  public void setDefaultErrorHandlerName(String defaultErrorHandlerName) {
    config.setDefaultErrorHandlerName(defaultErrorHandlerName);
  }

  public void setDefaultObjectSerializer(ObjectSerializer objectSerializer) {
    config.setDefaultObjectSerializer(objectSerializer);
  }

  public void setMaxQueueTransactionFilesSize(int queueTransactionFilesSizeInMegabytes) {
    config.setMaxQueueTransactionFilesSize(queueTransactionFilesSizeInMegabytes);
  }

  public void setDynamicConfigExpiration(DynamicConfigExpiration dynamicConfigExpiration) {
    explicitDynamicConfigExpiration = true;
    config.setDynamicConfigExpiration(dynamicConfigExpiration);
  }

  public void setInheritIterableRepeatability(String inheritIterableRepeatability) {
    config.setInheritIterableRepeatability(inheritIterableRepeatability);
  }

  public void setCorrelationIdGeneratorExpression(String correlationIdGeneratorExpression) {
    config.setDefaultCorrelationIdGenerator(new ExpressionCorrelationIdGenerator(muleContext, correlationIdGeneratorExpression));
  }

  public void setExtensions(List<ConfigurationExtension> extensions) {
    config.addExtensions(extensions);
  }

  @Override
  public ZernovaConfiguration doGetObject() throws Exception {
    ZernovaConfiguration configuration = muleContext.getConfiguration();
    if (configuration instanceof DefaultZernovaConfiguration defaultConfig) {
      defaultConfig.setDefaultResponseTimeout(config.getDefaultResponseTimeout());
      defaultConfig.setDefaultTransactionTimeout(config.getDefaultTransactionTimeout());
      defaultConfig.setShutdownTimeout(shutdownTimeout
          .orElseGet(() -> {
            final var defaultValue = getProperty(GRACEFUL_SHUTDOWN_DEFAULT_TIMEOUT,
                                                 getenv(GRACEFUL_SHUTDOWN_DEFAULT_TIMEOUT.toUpperCase()
                                                     .replaceAll("\\.", "_")));

            if (defaultValue == null) {
              return 5000L;
            }

            return parseLong(defaultValue);
          }));
      defaultConfig.setDefaultErrorHandlerName(config.getDefaultErrorHandlerName());
      defaultConfig.addExtensions(config.getExtensions());
      defaultConfig.setMaxQueueTransactionFilesSize(config.getMaxQueueTransactionFilesSizeInMegabytes());
      defaultConfig.setDynamicConfigExpiration(resolveDynamicConfigExpiration());
      defaultConfig.setInheritIterableRepeatability(config.isInheritIterableRepeatability());
      config.getDefaultCorrelationIdGenerator().ifPresent(generator -> defaultConfig.setDefaultCorrelationIdGenerator(generator));
      applyDefaultIfNoObjectSerializerSet(defaultConfig);

      return configuration;
    } else {
      throw new ConfigurationException(createStaticMessage("Unable to set properties on read-only ZernovaConfiguration: "
          + configuration.getClass()));
    }
  }

  // This has to be done because the mule context object supplier has to be
  // used as a default if not dynamic configuration expiration is set in the app.
  // But this is accessible only after the mule context is started
  // (after the defaults for this class are resolved)
  private DynamicConfigExpiration resolveDynamicConfigExpiration() {
    if (explicitDynamicConfigExpiration) {
      return config.getDynamicConfigExpiration();
    }

    return DynamicConfigExpiration.getDefault(timeSupplier);
  }

  @Inject
  public void setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }

}
