/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.policy;

import static com.zernova.tck.probe.PollingProber.DEFAULT_POLLING_INTERVAL;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static reactor.core.publisher.Mono.just;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.policy.Policy;
import com.zernova.runtime.core.api.policy.PolicyChain;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.internal.event.InternalEvent;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.policy.api.PolicyPointcutParameters;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.probe.JUnitLambdaProbe;
import com.zernova.tck.probe.PollingProber;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

import org.reactivestreams.Publisher;

import org.junit.Before;
import org.junit.Test;

public class PolicyProcessorLeakTestCase extends AbstractZernovaTestCase {

  private static final int GC_POLLING_TIMEOUT = 10000;

  private Policy policy;

  @Before
  public void before() throws ZernovaException {
    final PolicyChain policyChain = new PolicyChain() {

      @Override
      public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
        // Skip the 'next' call
        return publisher;
      }
    };
    policy = new Policy(policyChain, "policyId");
  }

  @Override
  protected CoreEvent testEvent() throws ZernovaException {
    CoreEvent event = super.testEvent();
    ((InternalEvent) event).setSourcePolicyContext(new SourcePolicyContext(mock(PolicyPointcutParameters.class)));
    ((InternalEvent) event).setOperationPolicyContext(mock(OperationPolicyContext.class, RETURNS_DEEP_STUBS));
    return event;
  }

  @Test
  public void sourceNextOperationRefCleared() throws ZernovaException {
    Processor nextProcessor = new TestProcessor();
    final PhantomReference<Processor> processorRef = new PhantomReference<>(nextProcessor, new ReferenceQueue<>());
    SourcePolicyProcessor policyProcessor = new SourcePolicyProcessor(policy, nextProcessor);

    final CoreEvent event = testEvent();
    try {
      just(event).transform(policyProcessor).block();
      nextProcessor = null;
      policyProcessor = null;
      policy = null;
    } finally {
      ((BaseEventContext) event.getContext()).success();
    }

    probeNextRefGcd(processorRef);
  }

  @Test
  public void operationNextOperationRefCleared() throws ZernovaException {
    Processor nextProcessor = new TestProcessor();
    final PhantomReference<Processor> processorRef = new PhantomReference<>(nextProcessor, new ReferenceQueue<>());
    OperationPolicyProcessor policyProcessor = new OperationPolicyProcessor(policy, nextProcessor);

    final CoreEvent event = testEvent();
    try {
      just(event).transform(policyProcessor).block();
      nextProcessor = null;
      policyProcessor = null;
      policy = null;
    } finally {
      ((BaseEventContext) event.getContext()).success();
    }

    probeNextRefGcd(processorRef);
  }

  private void probeNextRefGcd(final PhantomReference<Processor> processorRef) {
    new PollingProber(GC_POLLING_TIMEOUT, DEFAULT_POLLING_INTERVAL).check(new JUnitLambdaProbe(() -> {
      System.gc();
      assertThat(processorRef.isEnqueued(), is(true));
      return true;
    }, "A hard reference is being mantained to the next processor."));
  }

  private static final class TestProcessor implements Processor {

    @Override
    public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
      return publisher;
    }

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      return event;
    }
  }
}
