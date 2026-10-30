/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation;

import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.MAX_REFRESH_ATTEMPTS;
import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.refreshTokenIfNecessary;
import static com.zernova.runtime.module.extension.internal.runtime.streaming.CursorResetInterceptor.CURSOR_RESET_HANDLER_VARIABLE;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.internal.exception.EnrichedErrorMapping;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.extension.api.connectivity.oauth.AccessTokenExpiredException;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor.ExecutorCallback;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.streaming.CursorResetHandler;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import java.util.List;

/**
 * A specialization of {@link OperationMessageProcessor} for operations which might be running with an OAuth enabled
 * {@link ConnectionProvider}.
 * <p>
 * If handles {@link AccessTokenExpiredException}s and executes the refresh token flow and retries accordingly.
 * <p>
 * If the operation was not configured with an OAuth enabled connection provider, then it behaves the same as its parent class
 *
 * @since 4.0
 */
public class OAuthOperationMessageProcessor extends OperationMessageProcessor {

  public OAuthOperationMessageProcessor(ExtensionModel extensionModel,
                                        OperationModel operationModel,
                                        ValueResolver<ConfigurationProvider> configurationProviderResolver,
                                        String target,
                                        String targetValue,
                                        List<EnrichedErrorMapping> errorMappings,
                                        ResolverSet resolverSet,
                                        CursorProviderFactory cursorProviderFactory,
                                        RetryPolicyTemplate retryPolicyTemplate,
                                        MessageProcessorChain nestedChain,
                                        ClassLoader classLoader,
                                        ExtensionManager extensionManager,
                                        ReflectionCache reflectionCache,
                                        ResultTransformer resultTransformer,
                                        long outerFluxTerminationTimeout) {
    super(extensionModel, operationModel, configurationProviderResolver, target, targetValue, errorMappings, resolverSet,
          cursorProviderFactory, retryPolicyTemplate, nestedChain, classLoader,
          extensionManager, reflectionCache, resultTransformer, outerFluxTerminationTimeout);
  }

  @Override
  protected void executeOperation(ExecutionContextAdapter<OperationModel> operationContext, ExecutorCallback callback) {
    super.executeOperation(operationContext, refreshable(operationContext, callback));
  }

  private ExecutorCallback refreshable(ExecutionContextAdapter<OperationModel> operationContext, ExecutorCallback callback) {
    return new ExecutorCallback() {

      private int attempts = 0;

      @Override
      public void complete(Object value) {
        callback.complete(value);
      }

      @Override
      public void error(Throwable e) {
        try {
          if (++attempts <= MAX_REFRESH_ATTEMPTS && refreshTokenIfNecessary(operationContext, e)) {
            resetCursors(operationContext);
            OAuthOperationMessageProcessor.super.executeOperation(operationContext, this);
          } else {
            callback.error(e);
          }
        } catch (Exception refreshException) {
          callback.error(refreshException);
        }
      }

      private void resetCursors(ExecutionContextAdapter<OperationModel> operationContext) {
        CursorResetHandler cursorResetHandler =
            operationContext.getVariable(CURSOR_RESET_HANDLER_VARIABLE);
        if (cursorResetHandler != null) {
          cursorResetHandler.resetCursors();
        }
      }
    };
  }
}
