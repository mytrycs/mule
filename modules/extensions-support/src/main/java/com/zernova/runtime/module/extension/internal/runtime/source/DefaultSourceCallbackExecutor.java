/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import static java.util.Optional.empty;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.rx.Exceptions.wrapFatal;
import static com.zernova.runtime.core.internal.util.rx.ImmediateScheduler.IMMEDIATE_SCHEDULER;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.BACK_PRESSURE_ACTION_CONTEXT_PARAM;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.SOURCE_CALLBACK_CONTEXT_PARAM;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.SOURCE_COMPLETION_CALLBACK_PARAM;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.execution.CompletableCallback;
import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.core.api.Injector;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.security.SecurityManager;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.extension.api.runtime.source.BackPressureAction;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCompletionCallback;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.loader.java.property.SourceCallbackModelProperty;
import com.zernova.runtime.module.extension.internal.runtime.DefaultExecutionContext;
import com.zernova.runtime.module.extension.internal.runtime.execution.GeneratedMethodComponentExecutor;
import com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils;
import com.zernova.sdk.api.runtime.source.SourceCallbackContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

/**
 * Implementation of {@link SourceCallbackExecutor} which uses a {@link GeneratedMethodComponentExecutor} to execute the callback
 * through a {@link Method}
 *
 * @since 4.3.0
 */
class DefaultSourceCallbackExecutor implements SourceCallbackExecutor {

  private final ExtensionModel extensionModel;
  private final Optional<ConfigurationInstance> configurationInstance;
  private final SourceModel sourceModel;
  private final CursorProviderFactory cursorProviderFactory;
  private final StreamingManager streamingManager;
  private final ArtifactEncoding artifactEncoding;
  private final ServerNotificationManager notificationManager;
  private final SecurityManager securityManager;
  private final boolean async;
  private final GeneratedMethodComponentExecutor<SourceModel> executor;
  private final Component component;
  private final ZernovaContext muleContext;

  /**
   * Creates a new instance
   *
   * @param extensionModel        the {@link ExtensionModel} of the owning component
   * @param configurationInstance an {@link Optional} {@link ConfigurationInstance} in case the component requires a config
   * @param sourceModel           the model of the {@code source}
   * @param source                a {@link Source} instance
   * @param method                the method to be executed
   * @param cursorProviderFactory the {@link CursorProviderFactory} that was configured on the owning source
   * @param streamingManager      the application's {@link StreamingManager}
   * @param component             the source {@link Component}
   * @param artifactEncoding      the {@link ArtifactEncoding} of the artifact of the executing component.
   * @param notificationManager   the {@link ServerNotificationManager} of the artifact of the executing component.
   * @param injector              the injector for the current artifact deployment
   * @param securityManager       the security manager used by the Mule instance to authenticate and authorise incoming and
   *                              outgoing event traffic and service invocations
   * @param sourceCallbackModel   the callback's model
   * @param muleContext           the current {@link ZernovaContext}
   */
  public DefaultSourceCallbackExecutor(ExtensionModel extensionModel,
                                       Optional<ConfigurationInstance> configurationInstance,
                                       SourceModel sourceModel,
                                       Object source,
                                       Method method,
                                       CursorProviderFactory cursorProviderFactory,
                                       StreamingManager streamingManager,
                                       Component component,
                                       ArtifactEncoding artifactEncoding,
                                       ServerNotificationManager notificationManager,
                                       Injector injector,
                                       SecurityManager securityManager,
                                       SourceCallbackModelProperty sourceCallbackModel,
                                       ZernovaContext muleContext) {

    this.extensionModel = extensionModel;
    this.configurationInstance = configurationInstance;
    this.sourceModel = sourceModel;
    this.cursorProviderFactory = cursorProviderFactory;
    this.streamingManager = streamingManager;
    this.component = component;
    this.artifactEncoding = artifactEncoding;
    this.notificationManager = notificationManager;
    this.securityManager = securityManager;
    executor =
        new GeneratedMethodComponentExecutor<>(getAllGroups(sourceModel, method, sourceCallbackModel), method, source);
    try {
      initialiseIfNeeded(executor, injector);
    } catch (InitialisationException e) {
      throw new ZernovaRuntimeException(e);
    }
    async = Stream.of(method.getParameterTypes()).anyMatch(ZernovaExtensionUtils::isSourceCompletionCallbackType);

    this.muleContext = muleContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(CoreEvent event, Map<String, Object> parameters, SourceCallbackContext context,
                      CompletableCallback<Void> callback) {
    if (async) {
      final ExecutionContext<SourceModel> executionContext =
          createExecutionContext(event, parameters, context, new CompletableSourceCompletionCallback(callback));
      try {
        executor.execute(executionContext);
      } catch (Throwable t) {
        callback.error(wrapFatal(t));
      }
    } else {
      try {
        executor.execute(createExecutionContext(event, parameters, context, null));
        callback.complete(null);
      } catch (Throwable t) {
        callback.error(wrapFatal(t));
      }
    }
  }

  private ExecutionContext<SourceModel> createExecutionContext(CoreEvent event,
                                                               Map<String, Object> parameters,
                                                               SourceCallbackContext callbackContext,
                                                               SourceCompletionCallback sourceCompletionCallback) {
    ExecutionContextAdapter<SourceModel> executionContext = new DefaultExecutionContext<>(extensionModel,
                                                                                          configurationInstance,
                                                                                          parameters,
                                                                                          sourceModel,
                                                                                          event,
                                                                                          artifactEncoding,
                                                                                          notificationManager,
                                                                                          cursorProviderFactory,
                                                                                          streamingManager,
                                                                                          component,
                                                                                          null,
                                                                                          IMMEDIATE_SCHEDULER,
                                                                                          empty(),
                                                                                          securityManager,
                                                                                          muleContext);

    executionContext.setVariable(SOURCE_CALLBACK_CONTEXT_PARAM, callbackContext);
    if (sourceCompletionCallback != null) {
      executionContext.setVariable(SOURCE_COMPLETION_CALLBACK_PARAM, sourceCompletionCallback);
    }

    callbackContext.<BackPressureAction>getVariable(BACK_PRESSURE_ACTION_CONTEXT_PARAM)
        .ifPresent(action -> executionContext.setVariable(BACK_PRESSURE_ACTION_CONTEXT_PARAM, action));

    return executionContext;
  }

  private List<ParameterGroupModel> getAllGroups(SourceModel model, Method method,
                                                 SourceCallbackModelProperty sourceCallbackModel) {
    List<ParameterGroupModel> callbackParameters = sourceCallbackModel.getOnSuccessMethod().filter(method::equals)
        .map(m -> sourceModel.getSuccessCallback().get().getParameterGroupModels())
        .orElseGet(() -> sourceCallbackModel.getOnErrorMethod().filter(method::equals)
            .map(m -> sourceModel.getErrorCallback().get().getParameterGroupModels())
            .orElseGet(() -> sourceModel.getTerminateCallback().get().getParameterGroupModels()));

    return ImmutableList.<ParameterGroupModel>builder()
        .addAll(model.getParameterGroupModels())
        .addAll(callbackParameters).build();
  }
}
