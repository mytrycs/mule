/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor;

import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.tck.junit4.AbstractZernovaTestCase.TEST_CONNECTOR_LOCATION;

import static java.util.Optional.empty;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.api.profiling.ProfilingService;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.exception.FlowExceptionHandler;
import com.zernova.runtime.core.internal.exception.OnErrorPropagateHandler;
import com.zernova.runtime.core.internal.profiling.DummyComponentTracerFactory;
import com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig;
import com.zernova.runtime.core.privileged.exception.TemplateOnErrorHandler;
import com.zernova.tck.testmodels.mule.TestTransactionFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

/**
 * Utils to create a {@link TryScope} fully configured.
 */
public class TryScopeTestUtils {

  private TryScopeTestUtils() {

  }

  /**
   * Creates a {@link TryScope} that could be transactional (with local or xa transaction), without timeout.
   */
  public static TryScope createTryScope(ZernovaContext muleContext,
                                        ProfilingService profilingService,
                                        Optional<Boolean> txType)
      throws ZernovaException {
    return createTryScope(muleContext, profilingService, txType, empty());
  }

  /**
   * Creates a {@link TryScope} that could be transactional (with local or xa transaction), with a given timeout.
   */
  public static TryScope createTryScope(ZernovaContext muleContext,
                                        ProfilingService profilingService,
                                        Optional<Boolean> txType,
                                        Optional<Integer> timeout)
      throws ZernovaException {
    TryScope scope = new TryScope();
    Map<QName, Object> annotations = new HashMap<>();
    annotations.put(LOCATION_KEY, TEST_CONNECTOR_LOCATION);
    scope.setAnnotations(annotations);
    if (txType.isPresent()) {
      scope.setTransactionConfig(createTransactionConfig("ALWAYS_BEGIN", txType.get(), timeout));
    } else {
      scope.setTransactionConfig(createTransactionConfig("INDIFFERENT", false, timeout));
    }
    scope.setComponentTracerFactory(new DummyComponentTracerFactory());
    scope.setExceptionListener(mock(FlowExceptionHandler.class));
    scope.setProfilingService(profilingService);
    scope.setMuleConfiguration(mock(ZernovaConfiguration.class));
    scope.setMuleContext(muleContext);
    scope.setNotificationDispatcher(mock(NotificationDispatcher.class));
    muleContext.getInjector().inject(scope);
    return scope;
  }

  private static ZernovaTransactionConfig createTransactionConfig(String action, boolean isXa, Optional<Integer> timeout) {
    ZernovaTransactionConfig transactionConfig = new ZernovaTransactionConfig();
    transactionConfig.setActionAsString(action);
    transactionConfig.setFactory(new TestTransactionFactory(isXa));
    transactionConfig.setTimeout(timeout.orElse(0));
    return transactionConfig;
  }

  private static ComponentLocation mockComponentLocation() {
    ComponentLocation cl = mock(ComponentLocation.class);
    when(cl.getLocation()).thenReturn("test/error-handler/0");
    when(cl.getRootContainerName()).thenReturn(TEST_CONNECTOR_LOCATION.getRootContainerName());
    when(cl.getParts()).thenReturn(TEST_CONNECTOR_LOCATION.getParts());
    return cl;
  }

  public static TemplateOnErrorHandler createPropagateErrorHandler() {
    TemplateOnErrorHandler handler = new OnErrorPropagateHandler();
    Map<QName, Object> annotations = new HashMap<>();
    annotations.put(LOCATION_KEY, mockComponentLocation());
    handler.setAnnotations(annotations);
    return handler;
  }

}
