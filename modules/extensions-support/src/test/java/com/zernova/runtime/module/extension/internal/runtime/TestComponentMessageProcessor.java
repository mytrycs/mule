/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime;

import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.internal.policy.PolicyManager;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.internal.runtime.operation.ComponentMessageProcessor;
import com.zernova.runtime.module.extension.internal.runtime.operation.ResultTransformer;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

public abstract class TestComponentMessageProcessor extends ComponentMessageProcessor<ComponentModel> {

  public TestComponentMessageProcessor(ExtensionModel extensionModel, ComponentModel componentModel,
                                       ConfigurationProvider configurationProvider, String target, String targetValue,
                                       ResolverSet resolverSet, CursorProviderFactory cursorProviderFactory,
                                       RetryPolicyTemplate retryPolicyTemplate, MessageProcessorChain nestedChain,
                                       ClassLoader classLoader, ExtensionManager extensionManager,
                                       ReflectionCache reflectionCache, ResultTransformer resultTransformer,
                                       long terminationTimeout) {
    super(extensionModel, componentModel,
          configurationProvider != null ? new StaticValueResolver<>(configurationProvider) : null, target,
          targetValue,
          resolverSet, cursorProviderFactory,
          retryPolicyTemplate, nestedChain, classLoader, extensionManager, reflectionCache, resultTransformer,
          terminationTimeout);
  }
}
