/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.data.sample;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static com.zernova.runtime.core.internal.event.NullEventFactory.getNullEvent;
import static com.zernova.runtime.core.internal.util.rx.ImmediateScheduler.IMMEDIATE_SCHEDULER;
import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.withRefreshToken;
import static com.zernova.sdk.api.data.sample.SampleDataException.NOT_SUPPORTED;
import static com.zernova.sdk.api.data.sample.SampleDataException.UNKNOWN;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.EnrichableModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.Injector;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.retry.policy.NoRetryPolicyTemplate;
import com.zernova.runtime.core.api.security.SecurityManager;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.api.streaming.NullCursorProviderFactory;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.api.runtime.resolver.ParameterValueResolver;
import com.zernova.runtime.module.extension.api.tooling.sampledata.SampleDataProviderMediator;
import com.zernova.runtime.module.extension.internal.loader.java.property.SampleDataProviderFactoryModelProperty;
import com.zernova.runtime.module.extension.internal.runtime.DefaultExecutionContext;
import com.zernova.runtime.module.extension.internal.runtime.result.ReturnDelegate;
import com.zernova.runtime.module.extension.internal.runtime.result.ValueReturnDelegate;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.sdk.api.data.sample.SampleDataException;
import com.zernova.sdk.api.data.sample.SampleDataProvider;
import com.zernova.sdk.api.runtime.operation.Result;

import java.util.function.Supplier;

/**
 * Coordinates all the moving parts necessary to provision and execute a {@link SampleDataProvider}, handling possible errors and
 * transforming the output into a {@link Message}
 *
 * @since 4.4.0
 */
public class DefaultSampleDataProviderMediator implements SampleDataProviderMediator {

  private final ExtensionModel extensionModel;
  private final ComponentModel componentModel;
  private final Component component;
  private final ReflectionCache reflectionCache;
  private final ExpressionManager expressionManager;
  private final StreamingManager streamingManager;
  private final SampleDataProviderFactoryModelProperty sampleDataProperty;
  private final ReturnDelegate returnDelegate;
  private final ArtifactEncoding artifactEncoding;
  private final ServerNotificationManager notificationManager;
  private final CursorProviderFactory cursorProviderFactory = new NullCursorProviderFactory();
  private final Injector injector;
  private final ZernovaContext muleContext;

  /**
   * Creates a new instance of the mediator
   *
   * @param componentModel container model which is a {@link ParameterizedModel} and {@link EnrichableModel}
   * @param muleContext    context to be able to initialize {@link SampleDataProvider} if necessary
   */
  public DefaultSampleDataProviderMediator(ExtensionModel extensionModel,
                                           ComponentModel componentModel,
                                           Component component,
                                           ArtifactEncoding artifactEncoding,
                                           ServerNotificationManager notificationManager,
                                           ReflectionCache reflectionCache,
                                           ExpressionManager expressionManager,
                                           StreamingManager streamingManager,
                                           Injector injector,
                                           ZernovaContext muleContext) {
    this.extensionModel = extensionModel;
    this.componentModel = componentModel;
    this.component = component;
    this.artifactEncoding = artifactEncoding;
    this.notificationManager = notificationManager;
    this.reflectionCache = reflectionCache;
    this.expressionManager = expressionManager;
    this.streamingManager = streamingManager;
    this.injector = injector;
    sampleDataProperty = componentModel.getModelProperty(SampleDataProviderFactoryModelProperty.class).orElse(null);
    returnDelegate = new ValueReturnDelegate(componentModel, artifactEncoding);

    this.muleContext = muleContext;
  }

  /**
   * Resolves the sample data
   *
   * @param parameterValueResolver parameter resolver required if the associated {@link SampleDataProvider} requires the value of
   *                               parameters from the same parameter container.
   * @param connectionSupplier     supplier of connection instances related to the container and used, if necessary, by the
   *                               {@link SampleDataProvider}
   * @param configurationSupplier  supplier of configuration instance related to the container and used, if necessary, by the
   *                               {@link SampleDataProvider}
   * @return a {@link Message} carrying the sample data
   * @throws SampleDataException if an error occurs resolving the sample data
   */
  @Override
  public Message getSampleData(ParameterValueResolver parameterValueResolver,
                               Supplier<Object> connectionSupplier,
                               Supplier<Object> configurationSupplier)
      throws SampleDataException {
    return getSampleData(parameterValueResolver, connectionSupplier, configurationSupplier, () -> null);
  }

  /**
   * Resolves the sample data
   *
   * @param parameterValueResolver     parameter resolver required if the associated {@link SampleDataProvider} requires the value
   *                                   of parameters from the same parameter container.
   * @param connectionSupplier         supplier of connection instances related to the container and used, if necessary, by the
   *                                   {@link SampleDataProvider}
   * @param configurationSupplier      supplier of configuration instance related to the container and used, if necessary, by the
   *                                   {@link SampleDataProvider}
   * @param connectionProviderSupplier the connection provider in charge of providing the connection given by the connection
   *                                   supplier.
   *
   * @return a {@link Message} carrying the sample data
   * @throws SampleDataException if an error occurs resolving the sample data
   */
  @Override
  public Message getSampleData(ParameterValueResolver parameterValueResolver,
                               Supplier<Object> connectionSupplier,
                               Supplier<Object> configurationSupplier,
                               Supplier<ConnectionProvider> connectionProviderSupplier)
      throws SampleDataException {
    if (sampleDataProperty == null) {
      throw new SampleDataException(
                                    format("Component '%s' does not support Sample Data resolution", componentModel.getName()),
                                    NOT_SUPPORTED);
    }

    try {
      SampleDataProviderFactory factory = sampleDataProperty.createFactory(
                                                                           parameterValueResolver,
                                                                           connectionSupplier,
                                                                           configurationSupplier,
                                                                           reflectionCache,
                                                                           expressionManager,
                                                                           injector,
                                                                           componentModel);

      SampleDataProvider provider = factory.createSampleDataProvider();
      Result result = withRefreshToken(connectionProviderSupplier, () -> provider.getSample());

      return returnDelegate.asReturnValue(result, createExecutionContext(configurationSupplier)).getMessage();
    } catch (SampleDataException e) {
      throw e;
    } catch (Exception e) {
      throw new SampleDataException(format("An error occurred trying to obtain Sample Data for component '%s'. Cause: %s",
                                           componentModel.getName(), e.getMessage()),
                                    UNKNOWN, e);
    }
  }

  private ExecutionContextAdapter createExecutionContext(Supplier<Object> configurationSupplier) {
    return new DefaultExecutionContext(extensionModel,
                                       ofNullable(configurationSupplier.get()),
                                       emptyMap(),
                                       componentModel,
                                       getNullEvent(),
                                       artifactEncoding,
                                       notificationManager,
                                       cursorProviderFactory,
                                       streamingManager,
                                       component,
                                       new NoRetryPolicyTemplate(),
                                       IMMEDIATE_SCHEDULER,
                                       empty(),
                                       null,
                                       muleContext);
  }
}
