/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.policy;

import static com.zernova.functional.junit4.matchers.ThrowableCauseMatcher.hasCause;
import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType.UNKNOWN;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.rx.Exceptions.propagateWrappingFatal;
import static com.zernova.runtime.core.internal.event.EventQuickCopy.quickCopy;
import static com.zernova.runtime.core.internal.policy.PolicyNextActionMessageProcessor.POLICY_NEXT_OPERATION;
import static com.zernova.runtime.core.internal.policy.PolicyNextActionMessageProcessor.SOURCE_POLICY_PART_IDENTIFIER;
import static com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.fromSingleComponent;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Flux.deferContextual;
import static reactor.core.publisher.Mono.from;
import static reactor.core.publisher.Mono.just;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.TypedComponentIdentifier;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.context.notification.FlowStackElement;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.api.util.func.CheckedFunction;
import com.zernova.runtime.core.privileged.event.DefaultFlowCallStack;
import com.zernova.runtime.core.privileged.exception.MessagingException;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.OptionalInt;

import org.reactivestreams.Publisher;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SourcePolicyProcessorTestCase extends AbstractPolicyProcessorTestCase {

  @Rule
  public ExpectedException expected = none();

  @Override
  protected ReactiveProcessor getProcessor() {
    return new SourcePolicyProcessor(policy, flowProcessor);
  }

  @Test
  public void messageModifiedBeforeNextProcessorIsNotPropagatedToItWhenPropagationDisabled() throws ZernovaException {
    CoreEvent modifiedMessageEvent = CoreEvent.builder(initialEvent).message(MESSAGE).build();
    mockFlowReturningEvent(modifiedMessageEvent);
    when(policy.getPolicyChain().isPropagateMessageTransformations()).thenReturn(false);
    when(policy.getPolicyChain().apply(any()))
        .thenAnswer(invocation -> deferContextual(ctx -> Mono.<CoreEvent>from(invocation.getArgument(0))
            .transform((ReactiveProcessor) ((Reference) ctx.get(POLICY_NEXT_OPERATION)).get())));

    just(initialEvent).transform(policyProcessor).block();

    verify(flowProcessor).apply(eventCaptor.capture());
    assertEquals(((CoreEvent) from(eventCaptor.getValue()).block()).getMessage(), initialEvent.getMessage());
  }

  @Test
  public void handleErrorsWithFlowAsFailingComponent() throws ZernovaException {
    Exception expectedException = new NullPointerException("Expected");

    final PolicyNextActionMessageProcessor nextProcessor = new PolicyNextActionMessageProcessor();
    nextProcessor.setAnnotations(singletonMap(LOCATION_KEY,
                                              fromSingleComponent("policy")
                                                  .appendLocationPart("source", of(TypedComponentIdentifier.builder()
                                                      .identifier(ComponentIdentifier.builder()
                                                          .namespace("http-policy")
                                                          .namespaceUri("http://www.mulesoft.org/schema/mule/http-policy")
                                                          .name(SOURCE_POLICY_PART_IDENTIFIER).build())
                                                      .type(UNKNOWN)
                                                      .build()),
                                                                      empty(), OptionalInt.empty(), OptionalInt.empty())
                                                  .appendProcessorsPart()
                                                  .appendLocationPart("0", empty(), empty(), OptionalInt.empty(),
                                                                      OptionalInt.empty())));
    initialiseIfNeeded(nextProcessor, muleContext);

    when(operationPolicyContext.getOriginalEvent()).thenReturn(initialEvent);

    final Component flowAsComponent = mock(Component.class);
    when(flowAsComponent.getLocation()).thenReturn(fromSingleComponent("flow"));

    when(flowProcessor.apply(any())).thenAnswer(inv -> Flux.from((Publisher) inv.getArgument(0)).map(e -> {
      throw propagateWrappingFatal(new MessagingException(initialEvent, expectedException, flowAsComponent));
    }));
    // ReactiveProcessor flowProcessor = null;
    final CoreEvent event = quickCopy(initialEvent, singletonMap(POLICY_NEXT_OPERATION, new SoftReference<>(flowProcessor)));
    ((DefaultFlowCallStack) (event.getFlowCallStack()))
        .push(new FlowStackElement("policy", null, mock(ComponentLocation.class), emptyMap()));

    SourcePolicyContext sourcePolicyCtx = SourcePolicyContext.from(event);
    sourcePolicyCtx.configure(new MessageSourceResponseParametersProcessor() {

      @Override
      public CheckedFunction<CoreEvent, Map<String, Object>> getSuccessfulExecutionResponseParametersFunction() {
        return e -> emptyMap();
      }

      @Override
      public CheckedFunction<CoreEvent, Map<String, Object>> getFailedExecutionResponseParametersFunction() {
        return e -> emptyMap();
      }
    }, null);

    expected.expectCause(instanceOf(MessagingException.class));
    expected.expectCause(hasCause(sameInstance(expectedException)));

    just(event).transform(nextProcessor)
        .contextWrite(ctx -> ctx.put(POLICY_NEXT_OPERATION, new SoftReference<>(flowProcessor)))
        .block();
  }
}
