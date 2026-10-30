/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation;

import static java.util.Objects.requireNonNull;
import static com.zernova.runtime.api.util.collection.SmallMap.copy;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CONFIG_ATTRIBUTE_NAME;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getClassLoader;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.ExtensionComponent;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.ExtensionConnectionSupplier;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ParametersResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import java.util.Map;

/**
 * Base class for creating MessageProcessor instances of a given {@link ComponentModel}
 *
 * @since 4.0
 */
public abstract class ComponentMessageProcessorBuilder<M extends ComponentModel, P extends ExtensionComponent> {

  protected final ExtensionModel extensionModel;
  protected final M operationModel;
  protected final ReflectionCache reflectionCache;
  protected final ExpressionManager expressionManager;
  protected final ExtensionConnectionSupplier extensionConnectionSupplier;

  private final ZernovaContext muleContext;

  protected ConfigurationProvider configurationProvider;
  protected Map<String, ?> parameters;
  protected String target;
  protected String targetValue;
  protected CursorProviderFactory cursorProviderFactory;
  protected RetryPolicyTemplate retryPolicyTemplate;
  protected MessageProcessorChain nestedChain;
  protected ClassLoader classLoader;
  protected long terminationTimeout;

  public ComponentMessageProcessorBuilder(ExtensionModel extensionModel,
                                          M operationModel,
                                          ReflectionCache reflectionCache,
                                          ExpressionManager expressionManager,
                                          ExtensionConnectionSupplier extensionConnectionSupplier,
                                          ZernovaContext muleContext) {
    this.muleContext = requireNonNull(muleContext, "muleContext cannot be null");
    this.extensionModel = requireNonNull(extensionModel, "extensionModel cannot be null");
    this.operationModel = requireNonNull(operationModel, "operationModel cannot be null");
    this.reflectionCache = reflectionCache;
    this.extensionConnectionSupplier = extensionConnectionSupplier;
    this.expressionManager = expressionManager;
    this.terminationTimeout = muleContext.getConfiguration().getShutdownTimeout();
  }

  public P build() {
    return withContextClassLoader(getClassLoader(extensionModel), () -> {
      try {
        final ExtensionManager extensionManager = muleContext.getExtensionManager();
        final ResolverSet operationArguments = getArgumentsResolverSet();

        P processor = createMessageProcessor(extensionManager, operationArguments);
        // TODO: MULE-5002 this should not be necessary but lifecycle issues when injecting message processors automatically
        muleContext.getInjector().inject(processor);
        return processor;
      } catch (Exception e) {
        throw new ZernovaRuntimeException(e);
      }
    });
  }

  protected abstract P createMessageProcessor(ExtensionManager extensionManager, ResolverSet operationArguments);

  protected ResolverSet getArgumentsResolverSet() throws ConfigurationException {
    final ResolverSet parametersResolverSet =
        ParametersResolver
            .fromValues(parameters, muleContext, reflectionCache, expressionManager, operationModel.getName())
            .getParametersAsResolverSet(operationModel, muleContext);

    final ResolverSet childsResolverSet =
        ParametersResolver
            .fromValues(parameters, muleContext, reflectionCache, expressionManager, operationModel.getName())
            .getNestedComponentsAsResolverSet(operationModel);

    return parametersResolverSet.merge(childsResolverSet);
  }

  public ComponentMessageProcessorBuilder<M, P> setConfigurationProvider(ConfigurationProvider configurationProvider) {
    this.configurationProvider = configurationProvider;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setParameters(Map<String, ?> parameters) {
    this.parameters = copy(parameters);
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setTarget(String target) {
    this.target = target;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setTargetValue(String targetValue) {
    this.targetValue = targetValue;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setCursorProviderFactory(CursorProviderFactory cursorProviderFactory) {
    this.cursorProviderFactory = cursorProviderFactory;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setRetryPolicyTemplate(RetryPolicyTemplate retryPolicyTemplate) {
    this.retryPolicyTemplate = retryPolicyTemplate;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setNestedChain(MessageProcessorChain nestedChain) {
    this.nestedChain = nestedChain;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  public ComponentMessageProcessorBuilder<M, P> setTerminationTimeout(long terminationTimeout) {
    this.terminationTimeout = terminationTimeout;
    return this;
  }

  protected ValueResolver<ConfigurationProvider> getConfigurationProviderResolver() {
    // Uses the configurationProvider given to the builder if any, otherwise evaluates the parameters.
    return configurationProvider != null ? new StaticValueResolver<>(configurationProvider)
        : getConfigurationProviderResolver(parameters.get(CONFIG_ATTRIBUTE_NAME));
  }

  private ValueResolver<ConfigurationProvider> getConfigurationProviderResolver(Object configRefParameter) {
    if (configRefParameter instanceof ValueResolver) {
      return (ValueResolver<ConfigurationProvider>) configRefParameter;
    }

    if (configRefParameter instanceof ConfigurationProvider) {
      return new StaticValueResolver<>((ConfigurationProvider) configRefParameter);
    }

    return null;
  }
}
