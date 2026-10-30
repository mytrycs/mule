/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation;

import static com.zernova.runtime.api.component.AbstractComponent.ANNOTATION_NAME;
import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.api.component.AbstractComponent.ROOT_CONTAINER_NAME_KEY;
import static com.zernova.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;
import static com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType.FLOW;
import static com.zernova.runtime.api.config.FeatureFlaggingService.FEATURE_FLAGGING_SERVICE_KEY;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.api.processor.ReactiveProcessor.ProcessingType.CPU_INTENSIVE;
import static com.zernova.runtime.core.api.processor.ReactiveProcessor.ProcessingType.CPU_LITE;
import static com.zernova.runtime.core.api.processor.ReactiveProcessor.ProcessingType.CPU_LITE_ASYNC;
import static com.zernova.runtime.core.internal.policy.DefaultPolicyManager.noPolicyOperation;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.processToApply;
import static com.zernova.tck.ZernovaTestUtils.APPLE_FLOW;
import static com.zernova.tck.probe.PollingProber.probe;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.of;
import static java.util.concurrent.Executors.newFixedThreadPool;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static reactor.core.scheduler.Schedulers.fromExecutorService;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.TypedComponentIdentifier;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.EnrichableModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.ZernovaProperties;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.api.processor.ReactiveProcessor.ProcessingType;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.internal.policy.OperationParametersProcessor;
import com.zernova.runtime.core.internal.policy.PolicyManager;
import com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation;
import com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.DefaultLocationPart;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor.ExecutorCallback;
import com.zernova.runtime.feature.internal.config.DefaultFeatureFlaggingService;
import com.zernova.runtime.metadata.api.cache.MetadataCacheIdGeneratorFactory;
import com.zernova.runtime.module.extension.api.loader.java.property.CompletableComponentExecutorModelProperty;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.TestComponentMessageProcessor;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.namespace.QName;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import reactor.core.publisher.Mono;

@RunWith(Parameterized.class)
public class ComponentMessageProcessorPolicyProcessingStrategyTestCase extends AbstractZernovaContextTestCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(ComponentMessageProcessorPolicyProcessingStrategyTestCase.class);
  public static final String TEST_ARTIFACT_ID = "artifactId";

  @Parameters(name = "async: {0}, processingType: {1}")
  public static List<Object[]> parameters() {
    return asList(new Object[] {true, CPU_LITE_ASYNC},
                  new Object[] {false, CPU_INTENSIVE},
                  new Object[] {true, CPU_INTENSIVE});
  }

  private ComponentMessageProcessor<ComponentModel> processor;
  private Location mpRootContainerLocation;
  private FlowConstruct rootContainer;
  private ProcessingStrategy processingStrategy;

  private ExtensionModel extensionModel;
  private ComponentModel componentModel;

  private ResolverSet resolverSet;
  private ExtensionManager extensionManager;
  private PolicyManager policyManager;

  private final ExecutorService threadSwitcher = newFixedThreadPool(2);
  private final AssertingExecutionMediator mediator = new AssertingExecutionMediator();

  private final boolean async;
  private final ProcessingType processingType;

  public ComponentMessageProcessorPolicyProcessingStrategyTestCase(boolean async, ProcessingType processingType) {
    this.async = async;
    this.processingType = processingType;
  }

  @Override
  protected void doSetUpBeforeMuleContextCreation() throws Exception {
    super.doSetUpBeforeMuleContextCreation();
    policyManager = mock(PolicyManager.class);
  }

  @Override
  protected void addBuilders(List<ConfigurationBuilder> builders) {
    super.addBuilders(builders);
    builders.add(new AbstractConfigurationBuilder() {

      @Override
      protected void doConfigure(ZernovaContext muleContext) throws Exception {
        muleContext.getCustomizationService().overrideDefaultServiceImpl(FEATURE_FLAGGING_SERVICE_KEY,
                                                                         new DefaultFeatureFlaggingService(TEST_ARTIFACT_ID,
                                                                                                           emptyMap()));
        muleContext.getCustomizationService().overrideDefaultServiceImpl(ZernovaProperties.OBJECT_POLICY_MANAGER,
                                                                         policyManager);
      }
    });
  }

  @Before
  public void before() throws ZernovaException {
    CoreEvent response = testEvent();

    mpRootContainerLocation = mock(Location.class);

    extensionModel = mock(ExtensionModel.class);
    when(extensionModel.getXmlDslModel()).thenReturn(XmlDslModel.builder().setPrefix("mock").build());

    componentModel = mock(ComponentModel.class, withSettings().extraInterfaces(EnrichableModel.class));
    when((componentModel).getModelProperty(CompletableComponentExecutorModelProperty.class))
        .thenReturn(of(new CompletableComponentExecutorModelProperty((cp, p) -> (ctx, callback) -> callback.complete(response))));
    resolverSet = mock(ResolverSet.class);
    when(resolverSet.resolve(any(ValueResolvingContext.class))).thenReturn(mock(ResolverSetResult.class));

    extensionManager = mock(ExtensionManager.class);

    processor = new TestComponentMessageProcessor(extensionModel,
                                                  componentModel, null, null, null,
                                                  resolverSet, null, null, null,
                                                  null, extensionManager,
                                                  null, null,
                                                  muleContext.getConfiguration().getShutdownTimeout()) {

      @Override
      protected void validateOperationConfiguration(ConfigurationProvider configurationProvider) {}

      @Override
      public ProcessingType getInnerProcessingType() {
        return processingType;
      }

      @Override
      protected ExecutionMediator createExecutionMediator() {
        return mediator;
      }

      @Override
      public Location getRootContainerLocation() {
        return mpRootContainerLocation;
      }

      @Override
      protected boolean requiresConfig() {
        return async;
      }

    };

    rootContainer = mock(FlowConstruct.class);
    processingStrategy = mock(ProcessingStrategy.class);
    when(processingStrategy.onProcessor(any(ReactiveProcessor.class))).thenAnswer(inv -> inv.getArgument(0));
    when(processingStrategy.configureInternalPublisher(any(Publisher.class))).thenAnswer(inv -> inv.getArgument(0));

    when(componentLocator.find(mpRootContainerLocation)).thenReturn(of(rootContainer));
    when(rootContainer.getProcessingStrategy()).thenReturn(processingStrategy);

    processor.setComponentLocator(componentLocator);
    processor.setCacheIdGeneratorFactory(of(mock(MetadataCacheIdGeneratorFactory.class)));

    final Map<QName, Object> annotations = new HashMap<>();
    final ComponentIdentifier opIdentifier = buildFromStringRepresentation("ns:op");
    annotations.put(LOCATION_KEY, new DefaultComponentLocation(of(APPLE_FLOW),
                                                               asList(new DefaultLocationPart(APPLE_FLOW,
                                                                                              of(TypedComponentIdentifier
                                                                                                  .builder()
                                                                                                  .identifier(opIdentifier)
                                                                                                  .type(FLOW).build()),
                                                                                              Optional.empty(),
                                                                                              OptionalInt.empty(),
                                                                                              OptionalInt.empty()))));
    annotations.put(ROOT_CONTAINER_NAME_KEY, APPLE_FLOW);
    annotations.put(ANNOTATION_NAME, opIdentifier);
    processor.setAnnotations(annotations);

    initialiseIfNeeded(processor, muleContext);
    startIfNeeded(processor);
  }

  @After
  public void after() throws ZernovaException {
    stopIfNeeded(processor);
    disposeIfNeeded(processor, LOGGER);
    threadSwitcher.shutdownNow();
  }

  private static class AssertingExecutionMediator implements ExecutionMediator {

    private Thread executionThread;
    private boolean executed;

    @Override
    public void execute(CompletableComponentExecutor executor,
                        ExecutionContextAdapter context,
                        ExecutorCallback callback) {
      executed = true;
      executionThread = currentThread();
      callback.complete("");
    }
  }

  @Test
  public void policyChangesThreadBefore() throws ZernovaException {
    AtomicReference<Thread> switchedRef = new AtomicReference<>();

    when(policyManager.createOperationPolicy(eq(processor), any(CoreEvent.class), any(OperationParametersProcessor.class)))
        .thenReturn((operationEvent, operationExecutionFunction, opParamProcessor, componentLocation, callback) -> {

          threadSwitcher.execute(() -> {
            switchedRef.set(currentThread());
            operationExecutionFunction.execute(opParamProcessor.getOperationParameters(), operationEvent, callback);
          });

        });

    processToApply(testEvent(), processingStrategy.onProcessor(processor));

    probe(() -> {
      assertThat(mediator.executed, is(true));
      assertThat(mediator.executionThread, sameInstance(switchedRef.get()));
      return true;
    });
  }

  @Test
  public void policyChangesThreadAfter() throws ZernovaException {
    when(policyManager.createOperationPolicy(eq(processor), any(CoreEvent.class), any(OperationParametersProcessor.class)))
        .thenReturn((operationEvent, operationExecutionFunction, opParamProcessor, componentLocation, callback) -> {
          operationExecutionFunction.execute(opParamProcessor.getOperationParameters(), operationEvent, new ExecutorCallback() {

            @Override
            public void error(Throwable e) {
              threadSwitcher.execute(() -> {
                callback.error(e);
              });
            }

            @Override
            public void complete(Object value) {
              threadSwitcher.execute(() -> {
                callback.complete(value);
              });
            }
          });
        });

    processToApply(testEvent(), processingStrategy.onProcessor(processor));

    probe(() -> {
      assertThat(mediator.executed, is(true));
      assertThat(mediator.executionThread, sameInstance(currentThread()));
      return true;
    });
  }

  @Test
  public void noPolicyProcessingStrategyChangesThreadBefore() throws ZernovaException {
    AtomicReference<Thread> switchedRef = new AtomicReference<>();

    when(processingStrategy.onProcessor(any(ReactiveProcessor.class)))
        .thenAnswer(inv -> {
          final ProcessingType processingType = inv.getArgument(0, ReactiveProcessor.class).getProcessingType();
          if (processingType.equals(CPU_LITE_ASYNC) || processingType.equals(CPU_LITE)) {
            return (ReactiveProcessor) t -> Mono.from(t)
                .transform(inv.getArgument(0, ReactiveProcessor.class));
          } else {
            return (ReactiveProcessor) t -> Mono.from(t)
                .publishOn(fromExecutorService(threadSwitcher))
                .doOnNext(e -> switchedRef.set(currentThread()))
                .transform(inv.getArgument(0, ReactiveProcessor.class));
          }
        });

    stopIfNeeded(processor);
    disposeIfNeeded(processor, LOGGER);
    initialiseIfNeeded(processor, muleContext);
    startIfNeeded(processor);

    when(policyManager.createOperationPolicy(eq(processor), any(CoreEvent.class), any(OperationParametersProcessor.class)))
        .thenReturn(noPolicyOperation());

    processToApply(testEvent(), processingStrategy.onProcessor(processor));
    probe(() -> {
      assertThat(mediator.executed, is(true));
      assertThat(mediator.executionThread, processingType.equals(CPU_LITE_ASYNC)
          ? sameInstance(currentThread())
          : sameInstance(switchedRef.get()));
      return true;
    });
  }

  @Test
  public void noPolicyProcessingStrategyChangesThreadAfter() throws ZernovaException {
    when(processingStrategy.onProcessor(any(ReactiveProcessor.class)))
        .thenAnswer(inv -> {
          final ProcessingType processingType = inv.getArgument(0, ReactiveProcessor.class).getProcessingType();
          if (processingType.equals(CPU_LITE_ASYNC)) {
            return (ReactiveProcessor) t -> Mono.from(t)
                .transform(inv.getArgument(0, ReactiveProcessor.class))
                .publishOn(fromExecutorService(threadSwitcher));
          } else {
            return (ReactiveProcessor) t -> Mono.from(t)
                .transform(inv.getArgument(0, ReactiveProcessor.class));
          }
        });

    stopIfNeeded(processor);
    disposeIfNeeded(processor, LOGGER);
    initialiseIfNeeded(processor, muleContext);
    startIfNeeded(processor);

    when(policyManager.createOperationPolicy(eq(processor), any(CoreEvent.class), any(OperationParametersProcessor.class)))
        .thenReturn(noPolicyOperation());

    processToApply(testEvent(), processingStrategy.onProcessor(processor));
    probe(() -> {
      assertThat(mediator.executed, is(true));
      assertThat(mediator.executionThread, sameInstance(currentThread()));
      return true;
    });
  }

  @Test
  public void processingStrategyChangesThreadBefore() throws ZernovaException {
    AtomicReference<Thread> switchedRef = new AtomicReference<>();

    when(processingStrategy.onProcessor(any(ReactiveProcessor.class)))
        .thenAnswer(inv -> {
          final ProcessingType processingType = inv.getArgument(0, ReactiveProcessor.class).getProcessingType();
          if (processingType.equals(CPU_LITE_ASYNC) || processingType.equals(CPU_LITE)) {
            return (ReactiveProcessor) t -> Mono.from(t)
                .transform(inv.getArgument(0, ReactiveProcessor.class));
          } else {
            return (ReactiveProcessor) t -> Mono.from(t)
                .publishOn(fromExecutorService(threadSwitcher))
                .doOnNext(e -> switchedRef.set(currentThread()))
                .transform(inv.getArgument(0, ReactiveProcessor.class));
          }
        });

    stopIfNeeded(processor);
    disposeIfNeeded(processor, LOGGER);
    initialiseIfNeeded(processor, muleContext);
    startIfNeeded(processor);

    when(policyManager.createOperationPolicy(eq(processor), any(CoreEvent.class), any(OperationParametersProcessor.class)))
        .thenReturn((operationEvent, operationExecutionFunction, opParamProcessor, componentLocation, callback) -> {
          operationExecutionFunction.execute(opParamProcessor.getOperationParameters(), operationEvent, callback);
        });

    processToApply(testEvent(), processingStrategy.onProcessor(processor));
    probe(() -> {
      assertThat(mediator.executed, is(true));
      assertThat(mediator.executionThread, processingType.equals(CPU_LITE_ASYNC)
          ? sameInstance(currentThread())
          : sameInstance(switchedRef.get()));
      return true;
    });
  }

  @Test
  public void processingStrategyChangesThreadAfter() throws ZernovaException {
    when(processingStrategy.onProcessor(any(ReactiveProcessor.class)))
        .thenAnswer(inv -> {
          final ProcessingType processingType = inv.getArgument(0, ReactiveProcessor.class).getProcessingType();
          if (processingType.equals(CPU_LITE_ASYNC)) {
            return (ReactiveProcessor) t -> Mono.from(t)
                .transform(inv.getArgument(0, ReactiveProcessor.class))
                .publishOn(fromExecutorService(threadSwitcher));
          } else {
            return (ReactiveProcessor) t -> Mono.from(t)
                .transform(inv.getArgument(0, ReactiveProcessor.class));
          }
        });

    stopIfNeeded(processor);
    disposeIfNeeded(processor, LOGGER);
    initialiseIfNeeded(processor, muleContext);
    startIfNeeded(processor);

    when(policyManager.createOperationPolicy(eq(processor), any(CoreEvent.class), any(OperationParametersProcessor.class)))
        .thenReturn((operationEvent, operationExecutionFunction, opParamProcessor, componentLocation, callback) -> {
          operationExecutionFunction.execute(opParamProcessor.getOperationParameters(), operationEvent, callback);
        });

    processToApply(testEvent(), processingStrategy.onProcessor(processor));
    probe(() -> {
      assertThat(mediator.executed, is(true));
      assertThat(mediator.executionThread, sameInstance(currentThread()));
      return true;
    });
  }

}
