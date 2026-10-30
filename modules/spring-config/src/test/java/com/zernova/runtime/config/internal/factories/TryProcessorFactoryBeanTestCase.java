/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.api.component.AbstractComponent.ROOT_CONTAINER_NAME_KEY;
import static com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType.FLOW;
import static com.zernova.runtime.api.tx.TransactionType.LOCAL;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.internal.transaction.ZernovaTransactionConfig.ACTION_INDIFFERENT_STRING;
import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServices;

import static java.util.Arrays.asList;
import static java.util.Optional.of;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.TypedComponentIdentifier;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.lifecycle.ZernovaLifecycleInterceptor;
import com.zernova.runtime.core.internal.processor.TryScope;
import com.zernova.runtime.core.internal.registry.SimpleRegistry;
import com.zernova.runtime.core.internal.transaction.TransactionFactoryLocator;
import com.zernova.runtime.core.privileged.registry.RegistrationException;
import com.zernova.runtime.core.privileged.transaction.TransactionConfig;
import com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation;
import com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.DefaultLocationPart;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TryProcessorFactoryBeanTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  private final ZernovaContext muleContextMock = mockContextWithServices();

  private SimpleRegistry registry;

  @Before
  public void setUp() throws RegistrationException, InitialisationException {
    registry = new SimpleRegistry(muleContextMock, new ZernovaLifecycleInterceptor());
    final var txFactoryLocator = new TransactionFactoryLocator();
    registry.inject(txFactoryLocator);
    registry.registerObject("txFactory", txFactoryLocator);
  }

  @Test
  public void doesNotFailWithNoProcessors() throws Exception {
    TryProcessorFactoryBean tryProcessorFactoryBean = new TryProcessorFactoryBean();
    tryProcessorFactoryBean.setTransactionalAction(ACTION_INDIFFERENT_STRING);
    tryProcessorFactoryBean.setTransactionType(LOCAL);

    final Map<QName, Object> annotations = new HashMap<>();
    annotations.put(LOCATION_KEY, new DefaultComponentLocation(of("root"),
                                                               asList(new DefaultLocationPart("root",
                                                                                              of(TypedComponentIdentifier
                                                                                                  .builder()
                                                                                                  .identifier(mock(ComponentIdentifier.class))
                                                                                                  .type(FLOW).build()),
                                                                                              Optional.empty(),
                                                                                              OptionalInt.empty(),
                                                                                              OptionalInt.empty()))));
    annotations.put(ROOT_CONTAINER_NAME_KEY, "root");
    tryProcessorFactoryBean.setAnnotations(annotations);
    registry.inject(tryProcessorFactoryBean);

    TryScope tryMessageProcessor = tryProcessorFactoryBean.getObject();
    when(muleContextMock.getConfiguration().getDefaultTransactionTimeout()).thenReturn(30000);
    initialiseIfNeeded(tryMessageProcessor, muleContextMock);
    tryMessageProcessor.start();
    TransactionConfig transactionConfig = tryMessageProcessor.getTransactionConfig();
    assertThat(transactionConfig.getTimeout(), is(30000));
  }

}
