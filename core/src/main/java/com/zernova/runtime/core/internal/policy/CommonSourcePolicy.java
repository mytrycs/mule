/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.policy;

import static java.lang.Runtime.getRuntime;
import static com.zernova.runtime.core.internal.policy.SourcePolicyContext.from;

import com.zernova.runtime.api.component.execution.CompletableCallback;
import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.util.rx.FluxSinkSupplier;
import com.zernova.runtime.core.internal.util.rx.RoundRobinFluxSinkSupplier;
import com.zernova.runtime.core.internal.util.rx.TransactionAwareFluxSinkSupplier;
import com.zernova.runtime.core.privileged.event.BaseEventContext;

import java.util.function.Supplier;

import reactor.core.publisher.FluxSink;

/**
 * Common behavior for flow dispatching, whether policies are applied or not.
 */
class CommonSourcePolicy {

  private final FluxSinkSupplier<CoreEvent> policySink;

  CommonSourcePolicy(Supplier<FluxSink<CoreEvent>> sinkFactory) {
    this.policySink =
        new TransactionAwareFluxSinkSupplier<>(sinkFactory,
                                               new RoundRobinFluxSinkSupplier<>(getRuntime().availableProcessors(), sinkFactory));
  }

  public void process(SourcePolicy sourcePolicy, CoreEvent sourceEvent,
                      MessageSourceResponseParametersProcessor respParamProcessor,
                      CompletableCallback<Either<SourcePolicyFailureResult, SourcePolicySuccessResult>> callback) {

    SourcePolicyContext ctx = from(sourceEvent);
    if (ctx != null) {
      ctx.configure(respParamProcessor, callback);
      // This prevents the source policy from being garbage collected when it still has inflight events.
      // See PolicyManager class for details.
      ctx.setPinnedSourcePolicy(sourcePolicy);
    }

    policySink.get().next(sourceEvent);
  }

  public void finishFlowProcessing(CoreEvent event, Either<SourcePolicyFailureResult, SourcePolicySuccessResult> result) {
    if (!((BaseEventContext) event.getContext()).isComplete()) {
      ((BaseEventContext) event.getContext()).success(event);
    }

    from(event).getProcessCallback().complete(result);
  }

  public void finishFlowProcessing(CoreEvent event,
                                   Either<SourcePolicyFailureResult, SourcePolicySuccessResult> result,
                                   Throwable error,
                                   SourcePolicyContext ctx) {
    if (!((BaseEventContext) event.getContext()).isComplete()) {
      ((BaseEventContext) event.getContext()).error(error);
    }

    ctx.getProcessCallback().complete(result);
  }

  public void dispose() {
    policySink.dispose();
  }

  public Disposable deferredDispose() {
    final FluxSinkSupplier<CoreEvent> sink = policySink;
    return () -> {
      sink.dispose();
    };
  }

}
