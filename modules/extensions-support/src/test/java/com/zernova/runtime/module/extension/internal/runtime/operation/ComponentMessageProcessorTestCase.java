/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.event.EventContextFactory.create;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.test.allure.AllureConstants.ExecutionEngineFeature.EXECUTION_ENGINE;
import static com.zernova.test.allure.AllureConstants.ExecutionEngineFeature.ExecutionEngineStory.REACTOR;

import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.Arrays.asList;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static reactor.core.publisher.Mono.from;
import static reactor.core.publisher.Mono.just;

import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.EnrichableModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.expression.ExpressionRuntimeException;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.internal.event.InternalEvent;
import com.zernova.runtime.core.internal.rx.FluxSinkRecorder;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.core.privileged.processor.MessageProcessors;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.metadata.api.cache.MetadataCacheIdGeneratorFactory;
import com.zernova.runtime.module.extension.api.loader.java.property.CompletableComponentExecutorModelProperty;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.TestComponentMessageProcessor;
import com.zernova.runtime.module.extension.internal.runtime.resolver.RouteBuilderValueResolver;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

@Feature(EXECUTION_ENGINE)
@Story(REACTOR)
@RunWith(Parameterized.class)
public class ComponentMessageProcessorTestCase extends AbstractZernovaContextTestCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(ComponentMessageProcessorTestCase.class);

  protected ComponentMessageProcessor<ComponentModel> processor;
  protected ExtensionModel extensionModel;
  protected ComponentModel componentModel;
  protected ResolverSet resolverSet;
  protected ExtensionManager extensionManager;

  // A cached flow for creating test events. It doesn't even have to contain the processor we are going to test, because we will
  // be sending the events directly through the processor, without using the flow.
  private Flow testFlow;

  private final boolean isWithinProcessToApply;

  @Parameterized.Parameters(name = "Is within process to apply: {0}")
  public static List<Object[]> parameters() {
    return asList(
                  new Object[] {false},
                  new Object[] {true});
  }

  public ComponentMessageProcessorTestCase(boolean isWithinProcessToApply) {
    this.isWithinProcessToApply = isWithinProcessToApply;
  }

  @Before
  public void before() throws ZernovaException {
    extensionModel = mock(ExtensionModel.class);
    when(extensionModel.getXmlDslModel()).thenReturn(XmlDslModel.builder().setPrefix("mock").build());

    componentModel = mock(ComponentModel.class, withSettings().extraInterfaces(EnrichableModel.class));
    when((componentModel).getModelProperty(CompletableComponentExecutorModelProperty.class))
        .thenReturn(of(new CompletableComponentExecutorModelProperty(IdentityExecutor::create)));
    resolverSet = mock(ResolverSet.class);

    extensionManager = mock(ExtensionManager.class);

    testFlow = getTestFlow(muleContext);
    initialiseIfNeeded(testFlow, muleContext);

    processor = createProcessor();
    processor.setAnnotations(getAppleFlowComponentLocationAnnotations());
    processor.setComponentLocator(componentLocator);
    processor.setCacheIdGeneratorFactory(of(mock(MetadataCacheIdGeneratorFactory.class)));
    processor.setMuleConfiguration(muleContext.getConfiguration());

    initialiseIfNeeded(processor, muleContext);

    // Since the initialization of the message processor reassigns its inner resolver set, we need to set the spy back.
    processor.resolverSet = resolverSet;

    startIfNeeded(processor);
  }

  @After
  public void after() throws ZernovaException {
    stopIfNeeded(processor);
    disposeIfNeeded(processor, LOGGER);

    stopIfNeeded(testFlow);
    disposeIfNeeded(testFlow, LOGGER);
  }

  protected ComponentMessageProcessor<ComponentModel> createProcessor() {
    return new TestComponentMessageProcessor(extensionModel,
                                             componentModel, null, null, null,
                                             resolverSet, null, null, null,
                                             null, extensionManager,
                                             null, null,
                                             muleContext.getConfiguration().getShutdownTimeout()) {

      @Override
      protected void validateOperationConfiguration(ConfigurationProvider configurationProvider) {}

      @Override
      public ProcessingType getInnerProcessingType() {
        return ProcessingType.CPU_LITE;
      }
    };
  }

  @Override
  protected CoreEvent.Builder getEventBuilder() {
    // Overrides to avoid creating a new test flow for each new test event
    return InternalEvent.builder(create(testFlow, TEST_CONNECTOR_LOCATION));
  }

  @Test
  public void happyPath() throws ZernovaException {
    final ResolverSetResult resolverSetResult = mock(ResolverSetResult.class);
    when(resolverSet.resolve(any(ValueResolvingContext.class))).thenReturn(resolverSetResult);
    assertNotNull(from(processor.apply(just(testEvent())))
        .contextWrite(context -> context.put(MessageProcessors.WITHIN_PROCESS_TO_APPLY, isWithinProcessToApply)).block());
  }

  @Test
  public void muleRuntimeExceptionInResolutionResult() throws ZernovaException {
    final Exception thrown = new ExpressionRuntimeException(createStaticMessage("Expected"));
    when(resolverSet.resolve(any(ValueResolvingContext.class))).thenThrow(thrown);
    assertMessagingExceptionCausedBy(thrown, isWithinProcessToApply);
  }

  @Test
  public void muleExceptionInResolutionResult() throws ZernovaException {
    final Exception thrown = new DefaultZernovaException(createStaticMessage("Expected"));
    when(resolverSet.resolve(any(ValueResolvingContext.class))).thenThrow(thrown);
    assertMessagingExceptionCausedBy(thrown, isWithinProcessToApply);
  }

  @Test
  public void runtimeExceptionInResolutionResult() throws ZernovaException {
    final Exception thrown = new NullPointerException("Expected");
    when(resolverSet.resolve(any(ValueResolvingContext.class))).thenThrow(thrown);
    assertMessagingExceptionCausedBy(thrown, isWithinProcessToApply);
  }

  private void assertMessagingExceptionCausedBy(Exception thrown, boolean isWithinProcessToApply) throws ZernovaException {
    final List<Throwable> processingErrors = new ArrayList<>();
    FluxSinkRecorder<CoreEvent> emitter = new FluxSinkRecorder<>();
    emitter.next(testEvent());
    emitter.complete();

    Flux<CoreEvent> processorFlux =
        Flux.create(emitter).transform(objectFlux -> processor.apply(objectFlux)).onErrorContinue((throwable, o) -> {
          processingErrors.add(throwable);
        });

    Subscriber<CoreEvent> assertingSubscriber = new BaseSubscriber<CoreEvent>() {

      @Override
      protected void hookOnComplete() {
        super.hookOnComplete();
        assertThat("Only one error should be returned has part of the event processing", processingErrors, hasSize(1));
        assertThat("Error should be wrapper in a MessagingException", processingErrors,
                   contains(is(instanceOf(MessagingException.class))));
        assertThat("Error is not the expected one", processingErrors,
                   contains(hasProperty("cause", equalTo(thrown))));
      }
    };

    processorFlux.contextWrite(context -> context.put(MessageProcessors.WITHIN_PROCESS_TO_APPLY, isWithinProcessToApply))
        .subscribeWith(assertingSubscriber);
  }

  @Test
  public void messageProcessorLifecycleIsPropagatedToRouteChains() throws ZernovaException {
    clearInvocations(resolverSet);

    Map<String, ValueResolver<?>> valueResolvers = new HashMap<>();
    RouteBuilderValueResolver routeBuilderValueResolver = mock(RouteBuilderValueResolver.class);
    valueResolvers.put("resolver", routeBuilderValueResolver);
    when(resolverSet.getResolvers()).thenReturn(valueResolvers);

    processor.stop();
    verify(routeBuilderValueResolver, times(1)).stop();

    processor.dispose();
    verify(routeBuilderValueResolver, times(1)).dispose();

    // initialization of the route chains is done within the initialization of the whole value resolvers set
    processor.initialise();
    verify(resolverSet, times(1)).initialise();

    processor.start();
    verify(routeBuilderValueResolver, times(1)).start();
  }

  @Test
  public void happyPathFluxPublisher() throws ZernovaException, InterruptedException {
    final ResolverSetResult resolverSetResult = mock(ResolverSetResult.class);
    when(resolverSet.resolve(any(ValueResolvingContext.class))).thenReturn(resolverSetResult);

    subscribeToParallelPublisherAndAwait(3);
  }

  @Test
  public void multipleUpstreamPublishers() throws ZernovaException, InterruptedException {
    final ResolverSetResult resolverSetResult = mock(ResolverSetResult.class);
    when(resolverSet.resolve(any(ValueResolvingContext.class))).thenReturn(resolverSetResult);

    InfiniteEmitter<CoreEvent> eventsEmitter = new InfiniteEmitter<>(this::newEvent);
    InfiniteEmitter<CoreEvent> eventsEmitter2 = new InfiniteEmitter<>(this::newEvent);
    ItemsConsumer<CoreEvent> eventsConsumer = new ItemsConsumer<>(10);
    ItemsConsumer<CoreEvent> eventsConsumer2 = new ItemsConsumer<>(3);

    Flux.create(eventsEmitter)
        .transform(processor)
        .contextWrite(context -> context.put(MessageProcessors.WITHIN_PROCESS_TO_APPLY, isWithinProcessToApply))
        .subscribe(eventsConsumer);

    Flux.create(eventsEmitter2)
        .transform(processor)
        .contextWrite(context -> context.put(MessageProcessors.WITHIN_PROCESS_TO_APPLY, isWithinProcessToApply))
        .subscribe(eventsConsumer2);

    eventsEmitter.start();
    eventsEmitter2.start();
    try {
      assertThat(eventsConsumer.await(RECEIVE_TIMEOUT, MILLISECONDS), is(true));
      assertThat(eventsConsumer2.await(RECEIVE_TIMEOUT, MILLISECONDS), is(true));
    } finally {
      eventsEmitter.stop();
      eventsEmitter2.stop();
    }
  }

  @Test
  @Issue("W-13563214")
  public void newSubscriptionAfterPreviousPublisherTermination() throws ZernovaException, InterruptedException {
    final ResolverSetResult resolverSetResult = mock(ResolverSetResult.class);
    when(resolverSet.resolve(any(ValueResolvingContext.class))).thenReturn(resolverSetResult);

    subscribeToParallelPublisherAndAwait(5);
    subscribeToParallelPublisherAndAwait(4);
  }

  private void subscribeToParallelPublisherAndAwait(int numEvents) throws InterruptedException {
    InfiniteEmitter<CoreEvent> eventsEmitter = new InfiniteEmitter<>(this::newEvent);
    ItemsConsumer<CoreEvent> eventsConsumer = new ItemsConsumer<>(numEvents);

    Flux.create(eventsEmitter)
        .transform(processor)
        .doOnNext(Assert::assertNotNull)
        .contextWrite(context -> context.put(MessageProcessors.WITHIN_PROCESS_TO_APPLY, isWithinProcessToApply))
        .subscribe(eventsConsumer);

    eventsEmitter.start();
    try {
      assertThat(eventsConsumer.await(RECEIVE_TIMEOUT, MILLISECONDS), is(true));
    } finally {
      eventsEmitter.stop();
    }
  }

}
