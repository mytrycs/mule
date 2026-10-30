/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.interceptor;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.reflections.ReflectionUtils.getFields;
import static org.reflections.ReflectionUtils.withType;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.processor.interceptor.a.ProcessorInterceptorFactoryA;
import com.zernova.runtime.core.internal.processor.interceptor.b.ProcessorInterceptorFactoryB;
import com.zernova.runtime.core.internal.processor.interceptor.c.ProcessorInterceptorFactoryC;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;


public class DefaultProcessorInterceptorManagerTestCase extends AbstractZernovaTestCase {

  private DefaultProcessorInterceptorManager manager;

  @Before
  public void before() throws IllegalArgumentException, IllegalAccessException {
    manager = new DefaultProcessorInterceptorManager();
    injectMockMuleContext(manager);
  }

  private void injectMockMuleContext(Object injectionTarget) throws IllegalAccessException {
    final Field contextField = getFields(DefaultProcessorInterceptorManager.class, withType(ZernovaContext.class)).iterator().next();
    contextField.setAccessible(true);
    contextField.set(injectionTarget, mock(ZernovaContext.class, RETURNS_DEEP_STUBS));
  }

  @Test
  public void noInterceptors() {
    assertThat(manager.getInterceptorFactories(), empty());
  }

  @Test
  public void interceptorsOrderedAsRegisterd() {
    final ProcessorInterceptorFactoryA intFactoryA = new ProcessorInterceptorFactoryA();
    final ProcessorInterceptorFactoryB intFactoryB = new ProcessorInterceptorFactoryB();
    final ProcessorInterceptorFactoryC intFactoryC = new ProcessorInterceptorFactoryC();
    manager.setInterceptorFactories(of(asList(intFactoryA, intFactoryB, intFactoryC)));

    assertThat(manager.getInterceptorFactories(), contains(intFactoryA, intFactoryB, intFactoryC));
  }

  @Test
  public void interceptorsOrdered() {
    final ProcessorInterceptorFactoryA intFactoryA = new ProcessorInterceptorFactoryA();
    final ProcessorInterceptorFactoryB intFactoryB = new ProcessorInterceptorFactoryB();
    final ProcessorInterceptorFactoryC intFactoryC = new ProcessorInterceptorFactoryC();
    manager.setInterceptorFactories(of(asList(intFactoryA, intFactoryB, intFactoryC)));

    manager.setInterceptorsOrder(of(() -> asList("com.zernova.runtime.core.internal.processor.interceptor.c",
                                                 "com.zernova.runtime.core.internal.processor.interceptor.b")));

    assertThat(manager.getInterceptorFactories(), contains(intFactoryC, intFactoryB, intFactoryA));
  }

  @Test
  public void interceptorsOrderedManyPerOrderItem() {
    final ProcessorInterceptorFactoryA intFactoryA1 = new ProcessorInterceptorFactoryA();
    final ProcessorInterceptorFactoryA intFactoryA2 = new ProcessorInterceptorFactoryA();
    final ProcessorInterceptorFactoryA intFactoryA3 = new ProcessorInterceptorFactoryA();
    final ProcessorInterceptorFactoryA intFactoryA4 = new ProcessorInterceptorFactoryA();
    final ProcessorInterceptorFactoryA intFactoryA5 = new ProcessorInterceptorFactoryA();

    final ProcessorInterceptorFactoryB intFactoryB1 = new ProcessorInterceptorFactoryB();
    final ProcessorInterceptorFactoryB intFactoryB2 = new ProcessorInterceptorFactoryB();
    final ProcessorInterceptorFactoryB intFactoryB3 = new ProcessorInterceptorFactoryB();
    final ProcessorInterceptorFactoryB intFactoryB4 = new ProcessorInterceptorFactoryB();
    final ProcessorInterceptorFactoryB intFactoryB5 = new ProcessorInterceptorFactoryB();

    final ProcessorInterceptorFactoryC intFactoryC1 = new ProcessorInterceptorFactoryC();
    final ProcessorInterceptorFactoryC intFactoryC2 = new ProcessorInterceptorFactoryC();
    final ProcessorInterceptorFactoryC intFactoryC3 = new ProcessorInterceptorFactoryC();
    final ProcessorInterceptorFactoryC intFactoryC4 = new ProcessorInterceptorFactoryC();
    final ProcessorInterceptorFactoryC intFactoryC5 = new ProcessorInterceptorFactoryC();

    manager.setInterceptorFactories(of(asList(intFactoryA1, intFactoryA2, intFactoryA3, intFactoryA4, intFactoryA5,
                                              intFactoryB1, intFactoryB2, intFactoryB3, intFactoryB4, intFactoryB5,
                                              intFactoryC1, intFactoryC2, intFactoryC3, intFactoryC4, intFactoryC5)));

    manager.setInterceptorsOrder(of(() -> asList("com.zernova.runtime.core.internal.processor.interceptor.c",
                                                 "com.zernova.runtime.core.internal.processor.interceptor.b")));

    assertThat(manager.getInterceptorFactories(), contains(intFactoryC1, intFactoryC2, intFactoryC3, intFactoryC4, intFactoryC5,
                                                           intFactoryB1, intFactoryB2, intFactoryB3, intFactoryB4, intFactoryB5,
                                                           intFactoryA1, intFactoryA2, intFactoryA3, intFactoryA4, intFactoryA5));
  }
}
