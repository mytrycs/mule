/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import static java.lang.String.format;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getSdkSourceFactory;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.toBackPressureAction;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.source.MessageSource.BackPressureStrategy;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.source.BackPressureAction;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.sdk.api.runtime.source.Source;

import java.util.Optional;

/**
 * A factory for {@link SourceAdapter} instances
 */
public class SourceAdapterFactory {

  private final ExtensionModel extensionModel;
  private final SourceModel sourceModel;
  private final ResolverSet sourceParameters;
  private final ResolverSet successCallbackParameters;
  private final ResolverSet errorCallbackParameters;
  private final CursorProviderFactory cursorProviderFactory;
  private final Optional<BackPressureAction> backPressureAction;
  private final ExpressionManager expressionManager;
  private final ZernovaContext muleContext;

  public SourceAdapterFactory(ExtensionModel extensionModel,
                              SourceModel sourceModel,
                              ResolverSet sourceParameters,
                              ResolverSet successCallbackParameters,
                              ResolverSet errorCallbackParameters,
                              CursorProviderFactory cursorProviderFactory,
                              BackPressureStrategy backPressureStrategy,
                              ExpressionManager expressionManager,
                              ZernovaContext muleContext) {
    this.extensionModel = extensionModel;
    this.sourceModel = sourceModel;
    this.sourceParameters = sourceParameters;
    this.successCallbackParameters = successCallbackParameters;
    this.errorCallbackParameters = errorCallbackParameters;
    this.cursorProviderFactory = cursorProviderFactory;
    this.backPressureAction = toBackPressureAction(backPressureStrategy);
    this.expressionManager = expressionManager;
    this.muleContext = muleContext;
  }

  /**
   * Creates a new {@link SourceAdapter}
   *
   * @param configurationInstance an {@link Optional} {@link ConfigurationInstance} in case the source requires a config
   * @param sourceCallbackFactory a {@link SourceCallbackFactory}
   *
   * @param restarting            indicates if the creation of the adapter was triggered after by a restart
   *
   * @return a new {@link SourceAdapter}
   */
  public SourceAdapter createAdapter(Optional<ConfigurationInstance> configurationInstance,
                                     SourceCallbackFactory sourceCallbackFactory,
                                     Component component,
                                     SourceConnectionManager connectionManager,
                                     boolean restarting) {
    Either<Source, com.zernova.runtime.extension.api.runtime.source.Source> source =
        getSdkSourceFactory(sourceModel).createMessageSource();
    try {
      SourceConfigurer sourceConfigurer = new SourceConfigurer(sourceModel, component.getLocation(), sourceParameters,
                                                               expressionManager, muleContext, restarting);
      Source sdkSource = sourceConfigurer.configure(source.getValue().get(), configurationInstance);
      return new SourceAdapter(extensionModel,
                               sourceModel,
                               sdkSource,
                               configurationInstance,
                               cursorProviderFactory,
                               sourceCallbackFactory,
                               component,
                               connectionManager,
                               sourceParameters,
                               successCallbackParameters,
                               errorCallbackParameters,
                               backPressureAction);
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage(format("Could not create generator for source '%s'",
                                                                sourceModel.getName())),
                                     e);
    }
  }

  public ResolverSet getSourceParameters() {
    return sourceParameters;
  }
}
