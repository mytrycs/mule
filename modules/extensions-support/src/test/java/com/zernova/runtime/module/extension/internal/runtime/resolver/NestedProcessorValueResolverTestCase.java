/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static java.util.Optional.of;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.nested.NestedChainModel;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.internal.event.InternalEvent;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.extension.api.runtime.route.Chain;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.loader.java.property.SdkApiDefinedModelProperty;
import com.zernova.runtime.module.extension.internal.runtime.execution.SdkInternalContext;
import com.zernova.runtime.module.extension.internal.runtime.operation.InputEventAware;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import io.qameta.allure.Issue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class NestedProcessorValueResolverTestCase extends AbstractZernovaContextTestCase {

  @Rule
  public MockitoRule mockitorule = MockitoJUnit.rule();

  @Mock(lenient = true)
  private MessageProcessorChain messageProcessor;

  @Mock
  private ExpressionManager expressionManager;

  @Mock
  private NestedChainModel chainModel;

  @Mock
  private StreamingManager streamingManager;

  @Before
  public void before() throws Exception {
    final CoreEvent testEvent = testEvent();
    ((InternalEvent) testEvent).setSdkInternalContext(new SdkInternalContext());
    when(messageProcessor.process(any(CoreEvent.class))).thenReturn(testEvent);
    when(messageProcessor.apply(any(Publisher.class))).thenReturn(Mono.just(testEvent));
  }

  @Test
  public void yieldsNestedProcessor() throws Exception {
    ProcessorChainValueResolver resolver = new ProcessorChainValueResolver(chainModel, messageProcessor, streamingManager);
    final CoreEvent event = testEvent();

    Chain nestedProcessor = (Chain) resolver.resolve(ValueResolvingContext.builder(event)
        .withExpressionManager(expressionManager)
        .build());

    nestedProcessor.process(result -> {
      assertThat(result.getOutput(), is(TEST_PAYLOAD));

      ArgumentCaptor<CoreEvent> captor = ArgumentCaptor.forClass(CoreEvent.class);
      try {
        verify(messageProcessor).process(captor.capture());
      } catch (ZernovaException e) {
        throw new RuntimeException(e);
      }

      CoreEvent capturedEvent = captor.getValue();
      assertThat(capturedEvent, is(event));
    }, (e, r) -> fail(e.getMessage()));
  }

  @Test
  public void alwaysGivesDifferentInstances() throws Exception {
    ProcessorChainValueResolver resolver = new ProcessorChainValueResolver(chainModel, messageProcessor, streamingManager);
    ValueResolvingContext ctx = ValueResolvingContext.builder(testEvent()).withExpressionManager(expressionManager).build();
    Chain resolved1 = (Chain) resolver.resolve(ctx);
    Chain resolved2 = (Chain) resolver.resolve(ctx);

    assertThat(resolved1, is(not(sameInstance(resolved2))));
  }

  @Test
  public void chainIsCalledAsNonBlocking() throws Exception {
    ProcessorChainValueResolver resolver = new ProcessorChainValueResolver(chainModel, messageProcessor, streamingManager);

    Chain resolve = (Chain) resolver.resolve(ValueResolvingContext.builder(testEvent())
        .withExpressionManager(expressionManager)
        .build());

    resolve.process(result -> {
    }, (t, r) -> {
    });

    verify(messageProcessor, times(1)).apply(any());
    verify(messageProcessor, times(0)).process(any());
  }

  @Test
  @Issue("W-16420215")
  public void sdkApiChain() throws Exception {
    when(chainModel.getModelProperty(SdkApiDefinedModelProperty.class)).thenReturn(of(SdkApiDefinedModelProperty.INSTANCE));

    ProcessorChainValueResolver resolver = new ProcessorChainValueResolver(chainModel, messageProcessor, streamingManager);
    CoreEvent event = testEvent();

    ValueResolvingContext ctx = ValueResolvingContext.builder(event).withExpressionManager(expressionManager).build();
    com.zernova.sdk.api.runtime.route.Chain chain = (com.zernova.sdk.api.runtime.route.Chain) resolver.resolve(ctx);

    assertThat(((InputEventAware) chain).getOriginalEvent(), is(sameInstance(event)));
    chain.process(result -> {
      assertThat(result.getOutput(), is(TEST_PAYLOAD));

      ArgumentCaptor<CoreEvent> captor = ArgumentCaptor.forClass(CoreEvent.class);
      try {
        verify(messageProcessor).process(captor.capture());
      } catch (ZernovaException e) {
        throw new RuntimeException(e);
      }

      CoreEvent capturedEvent = captor.getValue();
      assertThat(capturedEvent, is(event));
    }, (e, r) -> fail(e.getMessage()));
  }
}
