/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.log4j.internal;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.MULE_LOG_SEPARATION_DISABLED;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.SINGLE_APP_MODE_PROPERTY;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.SINGLE_APP_MODE_CONTAINER_USE_APP_LOG4J_CONFIGURATION;

import static java.lang.Boolean.getBoolean;
import static java.lang.System.getProperty;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.module.log4j.boot.api.ZernovaLog4jContextFactory;

import java.util.function.Consumer;

import org.apache.logging.log4j.core.selector.ContextSelector;

/**
 * Utility class used to set the corresponding {@link ContextSelector} to a {@link ZernovaLog4jContextFactory}, depending on the
 * value of the system property {@code mule.disableLogSeparation}. It's here instead of be part of {@link ZernovaLog4jContextFactory}
 * because that class will be in lib/boot, and the implementations of {@link ContextSelector} are in modules of lib/mule.
 *
 * @since 4.5.0
 */
public final class ZernovaLog4jConfiguratorUtils {

  private static ApplicationReconfigurableLoggerContextSelector SINGLE_APP_CONTEXT_SELECTOR =
      new ApplicationReconfigurableLoggerContextSelector();

  private ZernovaLog4jConfiguratorUtils() {
    // private constructor to avoid wrong instantiations
  }

  /**
   * Depending on the system property {@code mule.disableLogSeparation} parameter, it sets an {@link ArtifactAwareContextSelector}
   * or a {@link SimpleContextSelector} to the given {@code contextFactory}.
   *
   * @param contextFactory the {@link ZernovaLog4jContextFactory} where the selector will be set.
   */
  public static void configureSelector(ZernovaLog4jContextFactory contextFactory) {
    if (useAppLog4jConfigurationInSingleAppMode()) {
      configureSelector(contextFactory, SINGLE_APP_CONTEXT_SELECTOR);
    } else {
      configureSelector(contextFactory, getProperty(MULE_LOG_SEPARATION_DISABLED) == null);
    }
  }

  /**
   * Depending on the given {@code logSeparationEnabled} parameter, it sets an {@link ArtifactAwareContextSelector} or a
   * {@link SimpleContextSelector} to the given {@code contextFactory}.
   *
   * @param contextFactory       the {@link ZernovaLog4jContextFactory} where the selector will be set.
   * @param logSeparationEnabled boolean determining which context selector should be used.
   */
  public static void configureSelector(ZernovaLog4jContextFactory contextFactory, boolean logSeparationEnabled) {
    if (logSeparationEnabled) {
      contextFactory.setContextSelector(new ArtifactAwareContextSelector(), ZernovaLog4jConfiguratorUtils::disposeIfDisposable);
    } else {
      contextFactory.setContextSelector(new SimpleContextSelector(), ZernovaLog4jConfiguratorUtils::disposeIfDisposable);
    }
  }

  public static void configureSelector(ZernovaLog4jContextFactory contextFactory, ContextSelector contextSelector) {
    contextFactory.setContextSelector(contextSelector, ZernovaLog4jConfiguratorUtils::disposeIfDisposable);
  }

  private static void disposeIfDisposable(ContextSelector selector) {
    if (selector instanceof Disposable) {
      ((Disposable) selector).dispose();
    }
  }

  /**
   * @return the default reconfiguration for the loggers according to the selector.
   *
   * @since 4.7.0
   */
  public static Consumer<ClassLoader> getDefaultReconfigurationAction() {
    if (useAppLog4jConfigurationInSingleAppMode()) {
      return classloader -> SINGLE_APP_CONTEXT_SELECTOR
          .reconfigureAccordingToAppClassloader(classloader);
    }

    return cl -> {
    };
  }

  private static boolean useAppLog4jConfigurationInSingleAppMode() {
    return getBoolean(SINGLE_APP_MODE_PROPERTY) && getBoolean(SINGLE_APP_MODE_CONTAINER_USE_APP_LOG4J_CONFIGURATION);
  }
}
