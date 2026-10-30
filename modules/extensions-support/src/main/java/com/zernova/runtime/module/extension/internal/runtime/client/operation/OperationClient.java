/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.client.operation;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.ERROR_MAPPINGS;
import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.OUTPUT;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.internal.event.NullEventFactory.getNullEvent;
import static com.zernova.runtime.core.internal.profiling.DummyComponentTracerFactory.DUMMY_COMPONENT_TRACER_INSTANCE;
import static com.zernova.runtime.core.internal.util.rx.ImmediateScheduler.IMMEDIATE_SCHEDULER;
import static com.zernova.runtime.module.extension.internal.runtime.client.NullComponent.NULL_COMPONENT;
import static com.zernova.runtime.module.extension.internal.runtime.resolver.ParametersResolver.fromValues;
import static com.zernova.runtime.module.extension.internal.runtime.resolver.ResolverSetUtils.getResolverSetFromParameters;
import static com.zernova.runtime.module.extension.internal.util.InterceptorChainUtils.createConnectionInterceptorsChain;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getMemberName;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getPagingResultTransformer;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.supportsOAuth;
import static com.zernova.runtime.tracer.customization.api.InternalSpanNames.GET_CONNECTION_SPAN_NAME;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.artifact.ArtifactType;
import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ComponentModelVisitor;
import com.zernova.runtime.api.meta.model.ComponentVisibility;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.OutputModel;
import com.zernova.runtime.api.meta.model.data.sample.SampleDataProviderModel;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.error.ErrorModel;
import com.zernova.runtime.api.meta.model.nested.NestableElementModel;
import com.zernova.runtime.api.meta.model.notification.NotificationModel;
import com.zernova.runtime.api.meta.model.operation.ExecutionType;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.api.parameterization.ComponentParameterization;
import com.zernova.runtime.api.profiling.ProfilingDataProducer;
import com.zernova.runtime.api.profiling.type.context.ComponentThreadingProfilingEventContext;
import com.zernova.runtime.api.streaming.Cursor;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.api.streaming.bytes.CursorStream;
import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;
import com.zernova.runtime.api.streaming.object.CursorIterator;
import com.zernova.runtime.api.streaming.object.CursorIteratorProvider;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.util.func.CheckedFunction;
import com.zernova.runtime.core.internal.streaming.CursorProviderDecorator;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.extension.api.client.ExtensionsClient;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor.ExecutorCallback;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.DefaultExecutionContext;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.ExtensionConnectionSupplier;
import com.zernova.runtime.module.extension.internal.runtime.operation.DefaultExecutionMediator;
import com.zernova.runtime.module.extension.internal.runtime.operation.ExecutionMediator;
import com.zernova.runtime.module.extension.internal.runtime.operation.ResultTransformer;
import com.zernova.runtime.module.extension.internal.runtime.resolver.resolver.ValueResolverFactory;
import com.zernova.runtime.module.extension.internal.runtime.result.ValueReturnDelegate;
import com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.runtime.tracer.api.component.ComponentTracer;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;

/**
 * {@link ExtensionsClient} delegate class for executing extension operations
 *
 * @since 4.5.0
 */
public class OperationClient implements Lifecycle {

  private static final Logger LOGGER = getLogger(OperationClient.class);
  private static final NullProfilingDataProducer NULL_PROFILING_DATA_PRODUCER = new NullProfilingDataProducer();
  private static final String PARAMS_PROPERTY_NAME = "parameterization";
  private static final BiFunction<ParameterGroupModel, ParameterModel, Object> NULL_PARAMS_FUNCTION = (g, p) -> null;

  private final ExtensionModel extensionModel;
  private final OperationModel operationModel;
  private final ExecutionMediator<OperationModel> mediator;
  private final CompletableComponentExecutor<OperationModel> executor;
  private final ValueReturnDelegate returnDelegate;
  private final StreamingManager streamingManager;
  private final ExtensionManager extensionManager;
  private final ExpressionManager expressionManager;
  private final ReflectionCache reflectionCache;
  private final ZernovaContext muleContext;
  private final ResolverSet resolverSet;
  private final ArtifactEncoding artifactEncoding;

  private Optional<ConfigurationProvider> configurationProvider = null;

  /**
   * Fallback resolvers to use for parameters not explicitly defined in the input parameterization
   */
  private Map<String, ValueResolver<?>> absentParameterResolvers;

  public static OperationClient from(OperationKey key,
                                     ExtensionManager extensionManager,
                                     ExpressionManager expressionManager,
                                     ExtensionConnectionSupplier extensionConnectionSupplier,
                                     ErrorTypeRepository errorTypeRepository,
                                     StreamingManager streamingManager,
                                     ReflectionCache reflectionCache,
                                     ComponentTracerFactory<CoreEvent> componentTracerFactory,
                                     ZernovaContext muleContext,
                                     ZernovaConfiguration muleConfiguration,
                                     ArtifactEncoding artifactEncoding,
                                     NotificationDispatcher notificationDispatcher) {

    return new OperationClient(
                               key.getExtensionModel(),
                               key.getOperationModel(),
                               createExecutionMediator(
                                                       key,
                                                       extensionConnectionSupplier,
                                                       errorTypeRepository,
                                                       reflectionCache,
                                                       componentTracerFactory,
                                                       muleContext,
                                                       muleConfiguration,
                                                       notificationDispatcher),
                               ComponentExecutorResolver.from(key, extensionManager, expressionManager, reflectionCache),
                               new ValueReturnDelegate(key.getOperationModel(), artifactEncoding),
                               streamingManager,
                               extensionManager,
                               expressionManager,
                               reflectionCache,
                               muleContext,
                               artifactEncoding);
  }

  private OperationClient(ExtensionModel extensionModel,
                          OperationModel operationModel,
                          ExecutionMediator<OperationModel> mediator,
                          CompletableComponentExecutor<OperationModel> executor,
                          ValueReturnDelegate returnDelegate,
                          StreamingManager streamingManager,
                          ExtensionManager extensionManager,
                          ExpressionManager expressionManager,
                          ReflectionCache reflectionCache,
                          ZernovaContext muleContext,
                          ArtifactEncoding artifactEncoding) {
    this.extensionModel = extensionModel;
    this.operationModel = new FilteredOperationModel(operationModel);
    this.mediator = mediator;
    this.executor = executor;
    this.returnDelegate = returnDelegate;
    this.streamingManager = streamingManager;
    this.extensionManager = extensionManager;
    this.expressionManager = expressionManager;
    this.reflectionCache = reflectionCache;
    this.muleContext = muleContext;
    this.artifactEncoding = artifactEncoding;
    resolverSet = createResolverSet();
  }

  private ResolverSet createResolverSet() {
    ValueResolverFactory factory = new ValueResolverFactory() {

      @Override
      public Optional<ValueResolver> ofNullableParameter(BiFunction<ParameterGroupModel, ParameterModel, Object> params,
                                                         ParameterGroupModel parameterGroupModel, ParameterModel parameterModel,
                                                         CheckedFunction<Object, ValueResolver> resolverFunction) {

        return Optional.of(new ValueResolver() {

          @Override
          public Object resolve(ValueResolvingContext context) throws ZernovaException {
            ComponentParameterization parameterization = (ComponentParameterization) context.getProperty(PARAMS_PROPERTY_NAME);
            Object value = parameterization.getParameter(parameterGroupModel, parameterModel);

            ValueResolver delegate = value != null
                ? resolverFunction.apply(value)
                : absentParameterResolvers.get(getMemberName(parameterModel));

            return delegate != null ? delegate.resolve(context) : null;
          }

          @Override
          public boolean isDynamic() {
            return true;
          }
        });
      }
    };

    final ClassLoader originalContextClassLoader = currentThread().getContextClassLoader();
    final ClassLoader extensionClassLoader = ZernovaExtensionUtils.getClassLoader(extensionModel);

    currentThread().setContextClassLoader(extensionClassLoader);
    ResolverSet createdResolverSet;
    try {
      createdResolverSet = getResolverSetFromParameters(operationModel,
                                                        NULL_PARAMS_FUNCTION,
                                                        muleContext,
                                                        true,
                                                        reflectionCache,
                                                        expressionManager,
                                                        "",
                                                        factory, artifactEncoding);

      createdResolverSet.initialise();

      ResolverSet absentResolverSet = fromValues(emptyMap(),
                                                 muleContext,
                                                 true,
                                                 reflectionCache,
                                                 expressionManager,
                                                 "").getParametersAsResolverSet(operationModel, muleContext);
      absentResolverSet.initialise();
      absentParameterResolvers = absentResolverSet.getResolvers();
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage(e.getMessage()), e);
    } finally {
      currentThread().setContextClassLoader(originalContextClassLoader);
    }
    return createdResolverSet;
  }

  public <T, A> CompletableFuture<Result<T, A>> execute(OperationKey key, DefaultOperationParameterizer parameterizer) {
    boolean shouldCompleteEvent = false;
    CoreEvent contextEvent = parameterizer.getContextEvent().orElse(null);
    if (contextEvent == null) {
      contextEvent = getNullEvent();
      shouldCompleteEvent = true;
    }

    OperationModel keyOperationModel = key.getOperationModel();
    Optional<ConfigurationInstance> configurationInstance = getConfigurationInstance(key, contextEvent);

    final Map<String, Object> resolvedParams =
        resolveOperationParameters(keyOperationModel, configurationInstance, parameterizer, contextEvent);
    CursorProviderFactory<Object> cursorProviderFactory = parameterizer.getCursorProviderFactory(streamingManager);

    ExecutionContextAdapter<OperationModel> context = new DefaultExecutionContext<>(
                                                                                    key.getExtensionModel(),
                                                                                    configurationInstance,
                                                                                    resolvedParams,
                                                                                    keyOperationModel,
                                                                                    contextEvent,
                                                                                    artifactEncoding,
                                                                                    muleContext.getNotificationManager(),
                                                                                    cursorProviderFactory,
                                                                                    streamingManager,
                                                                                    NULL_COMPONENT,
                                                                                    parameterizer.getRetryPolicyTemplate(),
                                                                                    IMMEDIATE_SCHEDULER,
                                                                                    empty(),
                                                                                    muleContext.getSecurityManager(),
                                                                                    muleContext);

    return doExecute(context, shouldCompleteEvent);
  }

  private <T, A> CompletableFuture<Result<T, A>> doExecute(ExecutionContextAdapter<OperationModel> ctx,
                                                           boolean shouldCompleteEvent) {

    CompletableFuture<Result<T, A>> future = new CompletableFuture<>();
    ExecutorCallback callback = new ExecutorCallback() {

      @Override
      public void complete(Object value) {
        EventCompletingValue<Result<T, A>> result = asEventCompletingResult(value, ctx, shouldCompleteEvent);

        try {
          future.complete(result.value);
        } finally {
          if (result.shouldCompleteEvent) {
            ((BaseEventContext) ctx.getEvent().getContext()).success();
          }
        }
      }

      @Override
      public void error(Throwable e) {
        if (!(e instanceof MessagingException)) {
          e = new MessagingException(ctx.getEvent(), e);
        }
        try {
          future.completeExceptionally(e);
        } finally {
          if (shouldCompleteEvent) {
            ((BaseEventContext) ctx.getEvent().getContext()).error(e);
          }
        }
      }
    };

    mediator.execute(executor, ctx, callback);
    return future;
  }

  private Map<String, Object> resolveOperationParameters(OperationModel operationModel,
                                                         Optional<ConfigurationInstance> configurationInstance,
                                                         DefaultOperationParameterizer parameterizer,
                                                         CoreEvent event) {
    ComponentParameterization.Builder<OperationModel> paramsBuilder = ComponentParameterization.builder(operationModel);
    parameterizer.setValuesOn(paramsBuilder);

    ValueResolvingContext.Builder ctxBuilder = ValueResolvingContext.builder(event)
        .withProperty(PARAMS_PROPERTY_NAME, paramsBuilder.build())
        .acceptsNullValues(false);
    configurationInstance.ifPresent(ctxBuilder::withConfig);

    try (ValueResolvingContext ctx = ctxBuilder.build()) {
      return resolverSet.resolve(ctx).asMap();
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Exception found while evaluating parameters:" + e.getMessage()), e);
    }
  }

  private <T, A> EventCompletingValue<Result<T, A>> asEventCompletingResult(Object value,
                                                                            ExecutionContextAdapter<OperationModel> context,
                                                                            boolean shouldCompleteEvent) {

    Message message = returnDelegate.asReturnValue(value, context).getMessage();
    TypedValue payload = message.getPayload();
    TypedValue attributes = message.getAttributes();

    EventCompletingValue<Object> completingPayload = asEventCompletingValue(payload.getValue(), context, shouldCompleteEvent);
    shouldCompleteEvent = shouldCompleteEvent && completingPayload.shouldCompleteEvent;

    EventCompletingValue<Object> completingAttributes =
        asEventCompletingValue(attributes.getValue(), context, shouldCompleteEvent);
    shouldCompleteEvent = shouldCompleteEvent && completingAttributes.shouldCompleteEvent;

    Result<T, A> result = (Result<T, A>) Result.builder()
        .output(completingPayload.value)
        .mediaType(payload.getDataType().getMediaType())
        .attributes(completingAttributes.value)
        .attributesMediaType(attributes.getDataType().getMediaType())
        .build();

    return new EventCompletingValue<>(result, shouldCompleteEvent);
  }

  private EventCompletingValue<Object> asEventCompletingValue(Object value, ExecutionContextAdapter ctx,
                                                              boolean shouldCompleteEvent) {
    if (shouldCompleteEvent) {
      if (value instanceof CursorIteratorProvider cipValue) {
        return new EventCompletingValue<>(new EventCompletingCursorIteratorProviderDecorator(cipValue,
                                                                                             ctx.getEvent()),
                                          false);
      } else if (value instanceof CursorStreamProvider cspValue) {
        return new EventCompletingValue<>(new EventCompletingCursorStreamProviderDecorator(cspValue,
                                                                                           ctx.getEvent()),
                                          false);
      }
    }

    return new EventCompletingValue<>(value, shouldCompleteEvent);
  }

  private Optional<ConfigurationInstance> getConfigurationInstance(OperationKey key, CoreEvent contextEvent) {
    return getConfigurationProvider(key, contextEvent).map(config -> config.get(contextEvent));
  }

  private Optional<ConfigurationProvider> getConfigurationProvider(OperationKey key, CoreEvent contextEvent) {
    if (configurationProvider != null) {
      return configurationProvider;
    }

    synchronized (this) {
      if (configurationProvider == null) {
        final String configName = key.getConfigName();

        if (configName != null) {
          configurationProvider = of(extensionManager.getConfigurationProvider(configName)
              .map(configProvider -> {
                if (configProvider.getExtensionModel() != extensionModel) {
                  throw new IllegalArgumentException(format(
                                                            "A config of the '%s' extension was expected but one from '%s' was parameterized instead",
                                                            extensionModel.getName(),
                                                            configProvider.getExtensionModel().getName()));
                }
                return configProvider;
              })
              .orElseThrow(() -> new ZernovaRuntimeException(
                                                          createStaticMessage("No configuration [" + configName + "] found"))));
        } else {
          configurationProvider = extensionManager.getConfigurationProvider(extensionModel, operationModel, contextEvent);
        }
      }
    }

    return configurationProvider;
  }


  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(mediator, true, muleContext);
    initialiseIfNeeded(executor, true, muleContext);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(mediator);
    startIfNeeded(executor);
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(mediator);
    stopIfNeeded(executor);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(mediator, LOGGER);
    disposeIfNeeded(executor, LOGGER);
  }

  private static ExecutionMediator<OperationModel> createExecutionMediator(
                                                                           OperationKey key,
                                                                           ExtensionConnectionSupplier extensionConnectionSupplier,
                                                                           ErrorTypeRepository errorTypeRepository,
                                                                           ReflectionCache reflectionCache,
                                                                           ComponentTracerFactory<CoreEvent> componentTracerFactory,
                                                                           ZernovaContext muleContext,
                                                                           ZernovaConfiguration muleConfiguration,
                                                                           NotificationDispatcher notificationDispatcher) {

    final ExtensionModel extensionModel = key.getExtensionModel();
    final OperationModel operationModel = key.getOperationModel();
    ExecutionMediator<OperationModel> mediator = new DefaultExecutionMediator<>(
                                                                                extensionModel,
                                                                                operationModel,
                                                                                createConnectionInterceptorsChain(extensionModel,
                                                                                                                  operationModel,
                                                                                                                  extensionConnectionSupplier,
                                                                                                                  reflectionCache,
                                                                                                                  DUMMY_COMPONENT_TRACER_INSTANCE),
                                                                                errorTypeRepository,
                                                                                muleContext.getExecutionClassLoader(),
                                                                                muleConfiguration,
                                                                                ArtifactType.valueOf(muleContext.getArtifactType()
                                                                                    .name()),
                                                                                notificationDispatcher,
                                                                                getResultTransformer(extensionConnectionSupplier,
                                                                                                     extensionModel,
                                                                                                     operationModel,
                                                                                                     componentTracerFactory),
                                                                                NULL_PROFILING_DATA_PRODUCER,
                                                                                getOperationConnectionTracer(componentTracerFactory),
                                                                                false);

    try {
      initialiseIfNeeded(mediator, true, muleContext);
      startIfNeeded(mediator);

      return mediator;
    } catch (ZernovaException e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create mediator for operation " + key), e);
    }
  }

  private static ComponentTracer<CoreEvent> getOperationConnectionTracer(
                                                                         ComponentTracerFactory<CoreEvent> componentTracerFactory) {
    return componentTracerFactory
        .fromComponent(NULL_COMPONENT,
                       GET_CONNECTION_SPAN_NAME, "");
  }

  private static ResultTransformer getResultTransformer(ExtensionConnectionSupplier extensionConnectionSupplier,
                                                        ExtensionModel extensionModel, OperationModel operationModel,
                                                        ComponentTracerFactory<CoreEvent> componentTracerFactory) {
    return getPagingResultTransformer(operationModel,
                                      extensionConnectionSupplier,
                                      supportsOAuth(extensionModel), getOperationConnectionTracer(componentTracerFactory))
                                          .orElse(null);
  }

  private abstract static class EventCompletingCursorProviderDecorator<T extends Cursor> extends CursorProviderDecorator<T> {

    private final CoreEvent event;

    private EventCompletingCursorProviderDecorator(CursorProvider delegate, CoreEvent event) {
      super(delegate);
      this.event = event;
    }

    @Override
    public void close() {
      try {
        super.close();
      } finally {
        ((BaseEventContext) event.getContext()).success();
      }
    }
  }


  private static class EventCompletingCursorStreamProviderDecorator
      extends EventCompletingCursorProviderDecorator<CursorStream> implements CursorStreamProvider {

    public EventCompletingCursorStreamProviderDecorator(CursorStreamProvider delegate, CoreEvent event) {
      super(delegate, event);
    }
  }


  private static class EventCompletingCursorIteratorProviderDecorator
      extends EventCompletingCursorProviderDecorator<CursorIterator> implements CursorIteratorProvider {

    public EventCompletingCursorIteratorProviderDecorator(CursorIteratorProvider delegate, CoreEvent event) {
      super(delegate, event);
    }
  }


  private static class EventCompletingValue<T> {

    private final T value;
    private final boolean shouldCompleteEvent;

    private EventCompletingValue(T value, boolean shouldCompleteEvent) {
      this.value = value;
      this.shouldCompleteEvent = shouldCompleteEvent;
    }
  }


  private static class NullProfilingDataProducer
      implements ProfilingDataProducer<ComponentThreadingProfilingEventContext, CoreEvent> {

    private NullProfilingDataProducer() {}

    @Override
    public void triggerProfilingEvent(ComponentThreadingProfilingEventContext profilerEventContext) {
      // Nothing to do
    }

    @Override
    public void triggerProfilingEvent(CoreEvent sourceData,
                                      Function<CoreEvent, ComponentThreadingProfilingEventContext> transformation) {
      // Nothing to do
    }
  }

  private static class FilteredOperationModel implements OperationModel {

    private final OperationModel delegate;
    private final List<ParameterGroupModel> parameterGroupModels;
    private final List<ParameterModel> allParameters = new ArrayList<>(20);

    private FilteredOperationModel(OperationModel delegate) {
      this.delegate = delegate;
      parameterGroupModels = new ArrayList<>(delegate.getParameterGroupModels().size());
      for (ParameterGroupModel group : delegate.getParameterGroupModels()) {
        String name = group.getName();
        if (OUTPUT.equals(name) || ERROR_MAPPINGS.equals(name)) {
          continue;
        }
        parameterGroupModels.add(group);
        allParameters.addAll(group.getParameterModels());
      }
    }

    @Override
    public String getDescription() {
      return delegate.getDescription();
    }

    @Override
    public String getName() {
      return delegate.getName();
    }

    @Override
    public ComponentVisibility getVisibility() {
      return delegate.getVisibility();
    }

    @Override
    public List<? extends NestableElementModel> getNestedComponents() {
      return delegate.getNestedComponents();
    }

    @Override
    public boolean isTransactional() {
      return delegate.isTransactional();
    }

    @Override
    public boolean requiresConnection() {
      return delegate.requiresConnection();
    }

    @Override
    public boolean supportsStreaming() {
      return delegate.supportsStreaming();
    }

    @Override
    public <T extends ModelProperty> Optional<T> getModelProperty(Class<T> propertyType) {
      return delegate.getModelProperty(propertyType);
    }

    @Override
    public Set<ModelProperty> getModelProperties() {
      return delegate.getModelProperties();
    }

    @Override
    public OutputModel getOutput() {
      return delegate.getOutput();
    }

    @Override
    public OutputModel getOutputAttributes() {
      return delegate.getOutputAttributes();
    }

    @Override
    public Optional<SampleDataProviderModel> getSampleDataProviderModel() {
      return delegate.getSampleDataProviderModel();
    }

    @Override
    public Set<String> getSemanticTerms() {
      return delegate.getSemanticTerms();
    }

    @Override
    public Optional<DeprecationModel> getDeprecationModel() {
      return delegate.getDeprecationModel();
    }

    @Override
    public boolean isDeprecated() {
      return delegate.isDeprecated();
    }

    @Override
    public Optional<DisplayModel> getDisplayModel() {
      return delegate.getDisplayModel();
    }

    @Override
    public Set<ErrorModel> getErrorModels() {
      return delegate.getErrorModels();
    }

    @Override
    public Set<NotificationModel> getNotificationModels() {
      return delegate.getNotificationModels();
    }

    @Override
    public boolean isBlocking() {
      return delegate.isBlocking();
    }

    @Override
    public ExecutionType getExecutionType() {
      return delegate.getExecutionType();
    }

    @Override
    public void accept(ComponentModelVisitor visitor) {
      delegate.accept(visitor);
    }

    @Override
    public List<ParameterGroupModel> getParameterGroupModels() {
      return parameterGroupModels;
    }

    @Override
    public List<ParameterModel> getAllParameterModels() {
      return allParameters;
    }

    @Override
    public StereotypeModel getStereotype() {
      return delegate.getStereotype();
    }

    @Override
    public Optional<ZernovaVersion> getMinZernovaVersion() {
      return delegate.getMinZernovaVersion();
    }

    @Override
    public boolean equals(Object obj) {
      return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
      return delegate.hashCode();
    }
  }
}
