/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.client.source;

import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.api.util.StringUtils.isBlank;
import static com.zernova.runtime.module.extension.internal.runtime.resolver.ResolverSetUtils.getResolverSetFromComponentParameterization;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getClassLoader;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.toBackPressureStrategy;

import static java.util.Collections.emptySet;
import static java.util.Optional.empty;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.api.parameterization.ComponentParameterization;
import com.zernova.runtime.api.util.collection.SmallMap;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.source.MessageSource.BackPressureStrategy;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.internal.exception.MessagingExceptionResolver;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation;
import com.zernova.runtime.extension.api.client.ExtensionsClient;
import com.zernova.runtime.extension.api.client.source.SourceParameterizer;
import com.zernova.runtime.extension.api.client.source.SourceResultHandler;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.internal.runtime.client.params.BaseParameterizer;
import com.zernova.runtime.module.extension.internal.runtime.resolver.NullResolverSet;
import com.zernova.runtime.module.extension.internal.runtime.source.ExtensionMessageSource;
import com.zernova.runtime.module.extension.internal.runtime.source.SourceAdapterFactory;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;

import javax.inject.Inject;

/**
 * {@link ExtensionsClient} delegate class for creating and operating message sources
 *
 * @since 4.5.0
 */
public class SourceClient<T, A> implements Lifecycle {

  private static final Logger LOGGER = getLogger(SourceClient.class);

  private final ExtensionModel extensionModel;
  private final SourceModel sourceModel;
  private final Consumer<SourceParameterizer> sourceParameterizerConsumer;
  private final Consumer<SourceResultHandler<T, A>> handlerConsumer;
  private final ExtensionManager extensionManager;
  private final StreamingManager streamingManager;
  private final ErrorTypeLocator errorTypeLocator;
  private final ReflectionCache reflectionCache;
  private final ExpressionManager expressionManager;
  private final NotificationDispatcher notificationDispatcher;
  private final ZernovaContext muleContext;
  private final ClassLoader extensionClassloader;

  private ExtensionMessageSource source;
  private MessagingExceptionResolver messagingExceptionResolver;
  private Optional<ConfigurationProvider> configurationProvider = empty();

  @Inject
  private ArtifactEncoding artifactEncoding;

  public SourceClient(ExtensionModel extensionModel,
                      SourceModel sourceModel,
                      Consumer<SourceParameterizer> sourceParameterizerConsumer,
                      Consumer<SourceResultHandler<T, A>> handlerConsumer,
                      ExtensionManager extensionManager,
                      StreamingManager streamingManager,
                      ErrorTypeLocator errorTypeLocator,
                      ReflectionCache reflectionCache,
                      ExpressionManager expressionManager,
                      NotificationDispatcher notificationDispatcher,
                      ZernovaContext muleContext) {
    this.extensionModel = extensionModel;
    this.sourceModel = sourceModel;
    this.sourceParameterizerConsumer = sourceParameterizerConsumer;
    this.handlerConsumer = handlerConsumer;
    this.extensionManager = extensionManager;
    this.streamingManager = streamingManager;
    this.errorTypeLocator = errorTypeLocator;
    this.reflectionCache = reflectionCache;
    this.expressionManager = expressionManager;
    this.notificationDispatcher = notificationDispatcher;
    this.muleContext = muleContext;

    extensionClassloader = getClassLoader(extensionModel);
  }

  @Override
  public void initialise() throws InitialisationException {
    DefaultSourceParameterizer parameterizer = new DefaultSourceParameterizer();
    sourceParameterizerConsumer.accept(parameterizer);

    final BackPressureStrategy backPressureStrategy = toBackPressureStrategy(parameterizer.getBackPressureMode());
    final CursorProviderFactory cursorProviderFactory = parameterizer.getCursorProviderFactory(streamingManager);
    final SourceAdapterFactory sourceAdapterFactory = newSourceAdapterFactory(parameterizer,
                                                                              cursorProviderFactory,
                                                                              backPressureStrategy);

    configurationProvider = resolveConfigurationProvider(extensionManager, parameterizer);
    source = new ExtensionMessageSource(extensionModel,
                                        sourceModel,
                                        sourceAdapterFactory,
                                        configurationProvider.orElse(null),
                                        true,
                                        parameterizer.getRetryPolicyTemplate(),
                                        cursorProviderFactory,
                                        backPressureStrategy,
                                        extensionManager,
                                        notificationDispatcher,
                                        "");

    source.setAnnotations(SmallMap.of(LOCATION_KEY, DefaultComponentLocation.from(sourceModel.getName())));
    source.setListener(event -> event);
    initialiseIfNeeded(source, true, muleContext);
    source.setMessageProcessingManager(new ExtensionsClientMessageProcessingManager(this, handlerConsumer));
    messagingExceptionResolver = new MessagingExceptionResolver(source);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(source);
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(source);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(source, LOGGER);
  }

  ResolverSet toResolverSet(BaseParameterizer parameterizer, ParameterizedModel model) {
    ComponentParameterization.Builder paramsBuilder = ComponentParameterization.builder(model);
    parameterizer.setValuesOn(paramsBuilder);

    try {
      ResolverSet resolverSet = getResolverSetFromComponentParameterization(
                                                                            paramsBuilder.build(),
                                                                            muleContext,
                                                                            true,
                                                                            reflectionCache,
                                                                            expressionManager,
                                                                            "", artifactEncoding);

      resolverSet.initialise();
      return resolverSet;
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Exception creating ResolverSet: " + e.getMessage()), e);
    }
  }

  Optional<ConfigurationInstance> resolveConfigurationInstance(CoreEvent event) {
    return configurationProvider.map(provider -> provider.get(event));
  }

  ClassLoader getExtensionClassLoader() {
    return extensionClassloader;
  }

  SourceModel getSourceModel() {
    return sourceModel;
  }

  MessagingException asMessagingException(Throwable t, CoreEvent event) {
    MessagingException exception = t instanceof MessagingException
        ? (MessagingException) t
        : new MessagingException(createStaticMessage(t.getMessage()), event, t, source);
    return messagingExceptionResolver.resolve(exception, errorTypeLocator, emptySet());
  }

  private SourceAdapterFactory newSourceAdapterFactory(DefaultSourceParameterizer parameterizer,
                                                       CursorProviderFactory cursorProviderFactory,
                                                       BackPressureStrategy backPressureStrategy) {
    return new SourceAdapterFactory(extensionModel,
                                    sourceModel,
                                    toResolverSet(parameterizer, sourceModel),
                                    NullResolverSet.INSTANCE,
                                    NullResolverSet.INSTANCE,
                                    cursorProviderFactory,
                                    backPressureStrategy,
                                    expressionManager,
                                    muleContext);
  }

  private Optional<ConfigurationProvider> resolveConfigurationProvider(ExtensionManager extensionManager,
                                                                       DefaultSourceParameterizer parameterizer) {
    if (isBlank(parameterizer.getConfigRef())) {
      return empty();
    }

    Optional<ConfigurationProvider> cp = extensionManager.getConfigurationProvider(parameterizer.getConfigRef());
    if (!cp.isPresent()) {
      throw new IllegalArgumentException("No configuration registered for key '" + parameterizer.getConfigRef() + "'");
    }

    return cp;
  }
}
