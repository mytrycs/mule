/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.policy;

import static com.zernova.runtime.api.notification.PolicyNotification.PROCESS_END;
import static com.zernova.runtime.api.notification.PolicyNotification.PROCESS_START;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.buildNewChainWithListOfProcessors;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.processToApply;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import static reactor.core.publisher.Flux.from;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.notification.FlowStackElement;
import com.zernova.runtime.core.api.context.notification.ServerNotificationHandler;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.exception.BaseExceptionHandler;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.internal.policy.PolicyNotificationHelper;
import com.zernova.runtime.core.internal.rx.FluxSinkRecorder;
import com.zernova.runtime.core.privileged.event.DefaultFlowCallStack;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.reactivestreams.Publisher;

import jakarta.inject.Inject;

/**
 * Policy chain for handling the message processor associated to a policy.
 *
 * @since 4.0
 */
@NoExtend
public class PolicyChain extends AbstractComponent
    implements Initialisable, Startable, Stoppable, Disposable, Processor {

  @Inject
  private ZernovaContext muleContext;

  @Inject
  private ServerNotificationHandler notificationManager;

  @Inject
  private ComponentTracerFactory componentTracerFactory;

  private ProcessingStrategy processingStrategy;

  private List<Processor> processors;
  private MessageProcessorChain processorChain;

  private String flowStackEntryName;

  private PolicyNotificationHelper notificationHelper;

  private boolean propagateMessageTransformations;

  private Optional<Consumer<Exception>> onError = empty();

  public void setProcessors(List<Processor> processors) {
    this.processors = processors;
  }

  @Override
  public final void initialise() throws InitialisationException {
    processorChain =
        buildNewChainWithListOfProcessors(ofNullable(processingStrategy), processors, policyChainErrorHandler(),
                                          componentTracerFactory.fromComponent(this));

    initialiseIfNeeded(processorChain, muleContext);

    notificationHelper = new PolicyNotificationHelper(notificationManager, muleContext.getConfiguration().getId(), this);

    flowStackEntryName = getLocation().getLocation() + "[before next]";
  }

  public ProcessingStrategy getProcessingStrategy() {
    return processingStrategy;
  }

  public void setProcessingStrategy(ProcessingStrategy processingStrategy) {
    this.processingStrategy = processingStrategy;
  }

  // Keep an active sink while the policy chain is active to avoid premature stopping of the Schedulers in the PS when the policy
  // executes within a Mono
  // (ref: AbstractReactorStreamProcessingStrategy#stopSchedulersIfNeeded)
  private FluxSinkRecorder chainActiveSink;

  @Override
  public void start() throws ZernovaException {
    chainActiveSink = new FluxSinkRecorder();
    processingStrategy.registerInternalSink(chainActiveSink.flux(), flowStackEntryName);

    if (processorChain != null) {
      processorChain.start();
    }
  }

  @Override
  public void dispose() {
    if (processorChain != null) {
      processorChain.dispose();
    }
  }

  @Override
  public void stop() throws ZernovaException {
    if (processorChain != null) {
      processorChain.stop();
    }

    chainActiveSink.complete();
  }

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    return processToApply(event, processorChain);
  }

  @Override
  public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    return from(publisher)
        .doOnNext(pushBeforeNextFlowStackElement()
            .andThen(notificationHelper.notification(PROCESS_START)))
        .transform(processorChain)
        .doOnNext(popFlowFlowStackElement()
            .andThen(e -> notificationHelper.fireNotification(e, null,
                                                              PROCESS_END)));
  }

  public ZernovaContext getMuleContext() {
    return muleContext;
  }

  private BaseExceptionHandler policyChainErrorHandler() {
    return new BaseExceptionHandler() {

      @Override
      public void onError(Exception exception) {
        MessagingException t = (MessagingException) exception;
        notificationHelper.fireNotification(t.getEvent(), t, PROCESS_END);
        popFlowFlowStackElement().accept(t.getEvent());
        onError.ifPresent(onError -> onError.accept(t));
      }

      @Override
      public String toString() {
        return PolicyChain.class.getSimpleName() + ".errorHandler @ " + getLocation().getLocation();
      }
    };
  }

  private Consumer<CoreEvent> pushBeforeNextFlowStackElement() {
    return event -> ((DefaultFlowCallStack) event.getFlowCallStack())
        .push(new FlowStackElement(flowStackEntryName, getIdentifier(), null, getLocation(), getAnnotations()));
  }

  private Consumer<CoreEvent> popFlowFlowStackElement() {
    return event -> ((DefaultFlowCallStack) event.getFlowCallStack()).pop();
  }

  public boolean isPropagateMessageTransformations() {
    return propagateMessageTransformations;
  }

  public void setPropagateMessageTransformations(boolean propagateMessageTransformations) {
    this.propagateMessageTransformations = propagateMessageTransformations;
  }

  public PolicyChain onChainError(Consumer<Exception> onError) {
    this.onError = of(onError);
    return this;
  }
}
