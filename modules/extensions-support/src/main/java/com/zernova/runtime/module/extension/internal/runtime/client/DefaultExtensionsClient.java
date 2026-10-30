/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.client;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.UNSUPPORTED_EXTENSIONS_CLIENT_RUN_ASYNC;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CONFIG_ATTRIBUTE_NAME;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.internal.util.FunctionalUtils.withNullEvent;
import static com.zernova.runtime.extension.privileged.util.ComponentDeclarationUtils.isPagedOperation;
import static com.zernova.runtime.module.extension.internal.runtime.client.operation.OperationClient.from;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.findOperation;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.findSource;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;
import com.zernova.runtime.extension.api.client.ExtensionsClient;
import com.zernova.runtime.extension.api.client.OperationParameterizer;
import com.zernova.runtime.extension.api.client.OperationParameters;
import com.zernova.runtime.extension.api.client.source.SourceHandler;
import com.zernova.runtime.extension.api.client.source.SourceParameterizer;
import com.zernova.runtime.extension.api.client.source.SourceResultHandler;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.internal.client.ComplexParameter;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.client.operation.DefaultOperationParameterizer;
import com.zernova.runtime.module.extension.internal.runtime.client.operation.EventedOperationsParameterDecorator;
import com.zernova.runtime.module.extension.internal.runtime.client.operation.OperationClient;
import com.zernova.runtime.module.extension.internal.runtime.client.operation.OperationKey;
import com.zernova.runtime.module.extension.internal.runtime.client.source.DefaultSourceHandler;
import com.zernova.runtime.module.extension.internal.runtime.client.source.SourceClient;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.ExtensionConnectionSupplier;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.DefaultObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jakarta.inject.Inject;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import org.slf4j.Logger;


/**
 * This is the default implementation for a {@link ExtensionsClient}, it uses the {@link ExtensionManager} in the
 * {@link ZernovaContext} to search for the extension that wants to execute the operation from.
 * <p>
 * The concrete execution of the operation is handled by an {@link OperationClient} instance.
 * <p>
 * This implementation can only execute extensions that were built using the SDK, Smart Connectors operations can't be executed.
 *
 * @since 4.0
 */
public final class DefaultExtensionsClient implements ExtensionsClient, Initialisable, Disposable {

  private static final Logger LOGGER = getLogger(DefaultExtensionsClient.class);

  @Inject
  private ExtensionManager extensionManager;

  @Inject
  private ErrorTypeRepository errorTypeRepository;

  @Inject
  private ErrorTypeLocator errorTypeLocator;

  @Inject
  private ExtensionConnectionSupplier extensionConnectionSupplier;

  @Inject
  private ReflectionCache reflectionCache;

  @Inject
  private ExpressionManager expressionManager;

  @Inject
  private StreamingManager streamingManager;

  @Inject
  private NotificationDispatcher notificationDispatcher;

  @Inject
  private ComponentTracerFactory<CoreEvent> componentTracerFactory;

  @Inject
  private FeatureFlaggingService featureFlaggingService;

  @Inject
  private ZernovaContext muleContext;

  @Inject
  private ZernovaConfiguration muleConfiguration;

  @Inject
  private ArtifactEncoding artifactEncoding;

  private ExecutorService cacheShutdownExecutor;
  private LoadingCache<OperationKey, OperationClient> operationClientCache;
  private final Set<SourceClient> sourceClients = new HashSet<>();

  @Override
  public <T, A> CompletableFuture<Result<T, A>> execute(String extensionName,
                                                        String operationName,
                                                        Consumer<OperationParameterizer> parameters) {

    ExtensionModel extensionModel = findExtension(extensionName);
    OperationModel operationModel = findOperationModel(extensionModel, operationName);

    return doExecute(extensionModel, operationModel, parameters);
  }

  private <T, A> CompletableFuture<Result<T, A>> doExecute(ExtensionModel extensionModel,
                                                           OperationModel operationModel,
                                                           Consumer<OperationParameterizer> parameters) {
    DefaultOperationParameterizer parameterizer = new DefaultOperationParameterizer();
    parameters.accept(parameterizer);

    OperationKey key = new OperationKey(extensionModel, operationModel, parameterizer.getConfigRef());

    return operationClientCache.get(key).execute(key, parameterizer);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T, A> SourceHandler createSource(String extensionName,
                                           String sourceName,
                                           Consumer<SourceResultHandler<T, A>> handler,
                                           Consumer<SourceParameterizer> parameters) {
    final ExtensionModel extensionModel = findExtension(extensionName);
    final SourceModel sourceModel = findSourceModel(extensionModel, sourceName);

    SourceClient<T, A> sourceClient = new SourceClient<>(extensionModel,
                                                         sourceModel,
                                                         parameters,
                                                         handler,
                                                         extensionManager,
                                                         streamingManager,
                                                         errorTypeLocator,
                                                         reflectionCache,
                                                         expressionManager,
                                                         notificationDispatcher,
                                                         muleContext);

    try {
      initialiseIfNeeded(sourceClient, true, muleContext);
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Exception initializing source:" + e.getMessage()), e);
    }

    return new DefaultSourceHandler(sourceClient, () -> discard(sourceClient));
  }

  private void discard(SourceClient sourceClient) {
    synchronized (sourceClients) {
      try {
        stopAndDispose(sourceClient);
      } finally {
        sourceClients.remove(sourceClient);
      }
    }
  }

  private void stopAndDispose(SourceClient sourceClient) {
    try {
      sourceClient.stop();
    } catch (Exception e) {
      LOGGER.error("Exception found stopping source client: " + e.getMessage(), e);
    } finally {
      sourceClient.dispose();
    }
  }

  private LoadingCache<OperationKey, OperationClient> createOperationClientCache() {
    return Caffeine.newBuilder()
        // Since the removal listener runs asynchronously, force waiting for all cleanup tasks to be complete before proceeding
        // (and finalizing) the context disposal.
        // Ref: https://github.com/ben-manes/caffeine/issues/104#issuecomment-238068997
        .executor(cacheShutdownExecutor)
        .expireAfterAccess(5, MINUTES)
        .removalListener((key, client, cause) -> disposeClient((OperationKey) key, (OperationClient) client))
        .build(this::createOperationClient);
  }

  private OperationClient createOperationClient(OperationKey key) {
    OperationClient client = from(
                                  key,
                                  extensionManager,
                                  expressionManager,
                                  extensionConnectionSupplier,
                                  errorTypeRepository,
                                  streamingManager,
                                  reflectionCache,
                                  componentTracerFactory,
                                  muleContext,
                                  muleConfiguration,
                                  artifactEncoding,
                                  notificationDispatcher);

    try {
      initialiseIfNeeded(client);
      startIfNeeded(client);
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Exception found creating operation client: " + e.getMessage()), e);
    }

    return client;
  }

  private void disposeClient(OperationKey identifier, OperationClient client) {
    try {
      stopIfNeeded(client);
    } catch (Exception e) {
      LOGGER.error("Exception found trying to stop operation client for operation {}", identifier);
    } finally {
      disposeIfNeeded(client, LOGGER);
    }
  }

  private OperationModel findOperationModel(ExtensionModel extensionModel, String operationName) {
    return findOperation(extensionModel, operationName)
        .orElseThrow(() -> new ZernovaRuntimeException(createStaticMessage(format("No Operation [%s] Found", operationName))));
  }

  private SourceModel findSourceModel(ExtensionModel extensionModel, String sourceName) {
    return findSource(extensionModel, sourceName)
        .orElseThrow(() -> new ZernovaRuntimeException(createStaticMessage(format("No Source [%s] Found", sourceName))));
  }

  private ExtensionModel findExtension(String extensionName) {
    return extensionManager.getExtension(extensionName)
        .orElseThrow(() -> new ZernovaRuntimeException(createStaticMessage("No Extension [" + extensionName + "] Found")));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Deprecated
  public <T, A> CompletableFuture<Result<T, A>> executeAsync(String extensionName,
                                                             String operationName,
                                                             OperationParameters parameters) {
    // Prevent using code that relies on com.zernova.runtime.extension.internal.client.ComplexParameter
    if (featureFlaggingService.isEnabled(UNSUPPORTED_EXTENSIONS_CLIENT_RUN_ASYNC)) {
      throw new UnsupportedOperationException("executeAsync not supported. Ref ZernovaRuntimeFeature#UNSUPPORTED_EXTENSIONS_CLIENT_RUN_ASYNC");
    }

    final ExtensionModel extensionModel = findExtension(extensionName);
    final OperationModel operationModel = findOperationModel(extensionModel, operationName);

    return doExecute(
                     extensionModel,
                     operationModel,
                     parameterizer -> {
                       setContextEvent(parameterizer, parameters);
                       parameters.getConfigName().ifPresent(parameterizer::withConfigRef);
                       resolveLegacyParameters(parameterizer, parameters);
                       configureLegacyRepeatableStreaming(parameterizer, operationModel);
                     });
  }

  private void resolveLegacyParameters(OperationParameterizer parameterizer, OperationParameters legacyParameters) {
    resolveLegacyParameters(legacyParameters.get(), parameterizer::withParameter);
  }

  private void resolveLegacyParameters(Map<String, Object> parameters,
                                       BiConsumer<String, Object> resolvedValueConsumer) {
    parameters.forEach((paramName, value) -> {
      if (CONFIG_ATTRIBUTE_NAME.equals(paramName)) {
        return;
      }

      if (value instanceof ComplexParameter complex) {
        DefaultObjectBuilder<?> builder = new DefaultObjectBuilder<>(complex.getType(), reflectionCache);
        resolveLegacyParameters(complex.getParameters(), (propertyName, propertyValue) -> builder
            .addPropertyResolver(propertyName, new StaticValueResolver<>(propertyValue)));

        value = withNullEvent(event -> {
          try (ValueResolvingContext ctx = ValueResolvingContext.builder(event).build()) {
            return builder.build(ctx);
          } catch (ZernovaException e) {
            throw new ZernovaRuntimeException(createStaticMessage(format("Could not construct parameter [%s]", paramName)), e);
          }
        });
      }

      resolvedValueConsumer.accept(paramName, value);
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Deprecated
  public <T, A> Result<T, A> execute(String extension, String operation, OperationParameters params)
      throws ZernovaException {

    try {
      return (Result<T, A>) executeAsync(extension, operation, params).get();
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof ZernovaException muleException) {
        throw muleException;
      } else {
        throw new DefaultZernovaException(cause);
      }
    } catch (InterruptedException e) {
      currentThread().interrupt();
      throw new DefaultZernovaException(e);
    }
  }

  private void configureLegacyRepeatableStreaming(OperationParameterizer parameterizer, OperationModel operationModel) {
    if (isPagedOperation(operationModel)) {
      setDefaultRepeatableIterables(parameterizer);
    } else if (operationModel.supportsStreaming()) {
      setDefaultRepeatableStreaming(parameterizer);
    }
  }

  private void setDefaultRepeatableStreaming(OperationParameterizer parameterizer) {
    parameterizer.withDefaultRepeatableStreaming();
  }

  private void setDefaultRepeatableIterables(OperationParameterizer parameterizer) {
    parameterizer.withDefaultRepeatableIterables();
  }

  private void setContextEvent(OperationParameterizer parameterizer, OperationParameters parameters) {
    if (parameters instanceof EventedOperationsParameterDecorator eop) {
      parameterizer.inTheContextOf(eop.getContextEvent());
    }
  }

  @Override
  public void initialise() throws InitialisationException {
    cacheShutdownExecutor = new ShutdownExecutor();
    operationClientCache = createOperationClientCache();
  }

  @Override
  public void dispose() {
    synchronized (sourceClients) {
      sourceClients.forEach(this::stopAndDispose);
      sourceClients.clear();
    }

    if (operationClientCache != null) {
      operationClientCache.invalidateAll();
    }

    if (cacheShutdownExecutor != null) {
      cacheShutdownExecutor.shutdown();
      shutdownAndAwaitTermination(cacheShutdownExecutor, 5, SECONDS);
    }
  }

}
