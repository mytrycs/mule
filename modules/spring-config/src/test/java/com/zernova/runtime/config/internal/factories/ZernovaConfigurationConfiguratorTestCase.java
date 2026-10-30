/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.GRACEFUL_SHUTDOWN_DEFAULT_TIMEOUT;
import static com.zernova.test.allure.AllureConstants.LifecycleAndDependencyInjectionFeature.LIFECYCLE_AND_DEPENDENCY_INJECTION;
import static com.zernova.test.allure.AllureConstants.LifecycleAndDependencyInjectionFeature.GracefulShutdownStory.GRACEFUL_SHUTDOWN_STORY;

import static java.lang.System.clearProperty;
import static java.lang.System.setProperty;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThrows;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(LIFECYCLE_AND_DEPENDENCY_INJECTION)
@Story(GRACEFUL_SHUTDOWN_STORY)
public class ZernovaConfigurationConfiguratorTestCase extends AbstractZernovaTestCase {

  @Rule
  public SystemProperty gracefulShutdownDefaultTimeout = new SystemProperty(GRACEFUL_SHUTDOWN_DEFAULT_TIMEOUT, null);

  private ZernovaConfigurationConfigurator muleConfigurationConfigurator;

  @Before
  public void setUp() {
    muleConfigurationConfigurator = new ZernovaConfigurationConfigurator();
    final var muleContext = mock(ZernovaContext.class);
    when(muleContext.getConfiguration()).thenReturn(new DefaultZernovaConfiguration());
    muleConfigurationConfigurator.setMuleContext(muleContext);
  }

  @Test
  public void shutdownTimeoutConfigured() throws Exception {
    muleConfigurationConfigurator.setShutdownTimeout(100L);

    final var configuration = muleConfigurationConfigurator.getObject();
    assertThat(configuration.getShutdownTimeout(), is(100L));
  }

  @Test
  public void shutdownTimeoutDefaultByEnvironment() throws Exception {
    setProperty(GRACEFUL_SHUTDOWN_DEFAULT_TIMEOUT, "200");

    try {
      final var configuration = muleConfigurationConfigurator.getObject();
      assertThat(configuration.getShutdownTimeout(), is(200L));
    } finally {
      clearProperty(GRACEFUL_SHUTDOWN_DEFAULT_TIMEOUT);
    }
  }

  @Test
  public void shutdownTimeoutDefaultByEnvironmentInvalid() throws Exception {
    setProperty(GRACEFUL_SHUTDOWN_DEFAULT_TIMEOUT, "aaa");

    try {
      assertThrows(NumberFormatException.class, () -> muleConfigurationConfigurator.getObject());
    } finally {
      clearProperty(GRACEFUL_SHUTDOWN_DEFAULT_TIMEOUT);
    }
  }

  @Test
  public void shutdownTimeoutDefault() throws Exception {
    final var configuration = muleConfigurationConfigurator.getObject();
    assertThat(configuration.getShutdownTimeout(), is(5000L));
  }
}
