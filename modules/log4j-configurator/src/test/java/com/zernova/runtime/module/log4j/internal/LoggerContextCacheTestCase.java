/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.log4j.internal;

import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.test.allure.AllureConstants.Logging.LOGGING;
import static com.zernova.test.allure.AllureConstants.Logging.LoggingStory.CONTEXT_FACTORY;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.module.artifact.api.classloader.RegionClassLoader;
import com.zernova.tck.size.SmallTest;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
@Feature(LOGGING)
@Story(CONTEXT_FACTORY)
public class LoggerContextCacheTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  private ClassLoader currentClassLoader;
  private LoggerContextCache loggerContextCache;
  @Mock
  private RegionClassLoader regionClassLoader;
  @Mock
  private ArtifactAwareContextSelector contextSelector;

  @Before
  public void before() {
    loggerContextCache = new LoggerContextCache(contextSelector, currentClassLoader);
    currentClassLoader = Thread.currentThread().getContextClassLoader();
  }

  @After
  public void after() {
    loggerContextCache.dispose();
  }

  @Test
  public void whenRecursiveMuleLoggerContextInstantiationExpectRecursiveLoggerContextInstantiationExceptionAndRecovery() {
    LoggerContext expectedLoggerContext = mock(ZernovaLoggerContext.class);
    when(contextSelector.buildContext(currentClassLoader))
        .thenAnswer(invocation -> loggerContextCache.getLoggerContext(currentClassLoader))
        .thenReturn(expectedLoggerContext);
    LoggerContext actualLoggerContext;
    try {
      actualLoggerContext = loggerContextCache.getLoggerContext(currentClassLoader);
      fail("Recursive instantiation should throw RecursiveLoggerContextInstantiationException");
    } catch (RecursiveLoggerContextInstantiationException e) {
      actualLoggerContext = loggerContextCache.getLoggerContext(currentClassLoader);
    }
    assertThat("Invalid LoggerContext", actualLoggerContext, equalTo(expectedLoggerContext));
  }

  @Test
  public void whenMuleRuntimeExceptionDuringMuleLoggerContextInstantiationExpectRecovery() {
    LoggerContext expectedLoggerContext = mock(ZernovaLoggerContext.class);
    when(contextSelector.buildContext(currentClassLoader))
        .thenThrow(ZernovaRuntimeException.class)
        .thenAnswer(invocation -> expectedLoggerContext);
    LoggerContext actualLoggerContext = null;
    try {
      loggerContextCache.getLoggerContext(currentClassLoader);
    } catch (ZernovaRuntimeException mre) {
      actualLoggerContext = loggerContextCache.getLoggerContext(currentClassLoader);
    }
    assertThat("Invalid loggerContext", actualLoggerContext, equalTo(expectedLoggerContext));
  }

  @Test
  public void whenMuleLoggerContextInstantiationExpectCacheStore() {
    LoggerContext firstExpectedLoggerContext = mock(ZernovaLoggerContext.class);
    LoggerContext secondExpectedLoggerContext = mock(ZernovaLoggerContext.class);
    when(contextSelector.buildContext(currentClassLoader)).thenReturn(firstExpectedLoggerContext);
    when(contextSelector.buildContext(regionClassLoader)).thenReturn(secondExpectedLoggerContext);
    loggerContextCache.getLoggerContext(currentClassLoader);
    withContextClassLoader(regionClassLoader, () -> {
      loggerContextCache.getLoggerContext(regionClassLoader);
    });
    assertThat("Additional or missing LoggerContext instances found in cache", loggerContextCache.getAllLoggerContexts(),
               hasSize(2));
    assertThat(loggerContextCache.getAllLoggerContexts(), hasItem(firstExpectedLoggerContext));
    assertThat(loggerContextCache.getAllLoggerContexts(), hasItem(secondExpectedLoggerContext));
  }

}
