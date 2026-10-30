/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.el;

import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_DW_EXPRESSION_LANGUAGE_ADAPTER;
import static com.zernova.runtime.core.internal.execution.ClassLoaderInjectorInvocationHandler.createClassLoaderInjectorInvocationHandler;

import static java.util.Collections.emptyList;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.internal.el.DefaultBindingContextBuilder;
import com.zernova.runtime.core.internal.el.DefaultExpressionManager;
import com.zernova.runtime.core.internal.el.ExtendedExpressionLanguageAdaptor;
import com.zernova.runtime.core.privileged.el.GlobalBindingContextProvider;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates the default {@link com.zernova.runtime.core.api.el.ExpressionManager}
 * <p>
 * This factory creates a proxy on top of the real expression manager. That proxy is used to set the right classloader on the
 * current thread's context classloader before calling any method on the delegate object.
 *
 * @since 4.0
 */
public class DefaultExpressionManagerFactoryBean implements FactoryBean<ExtendedExpressionManager> {

  private static final Logger LOGGER = getLogger(DefaultExpressionManagerFactoryBean.class);

  @Inject
  private ZernovaContext muleContext;

  @Inject
  @Named(OBJECT_DW_EXPRESSION_LANGUAGE_ADAPTER)
  private ExtendedExpressionLanguageAdaptor dwExpressionLanguage;

  @Inject
  // Avoid this to fail on unit tests with an incomplete repository
  @Autowired(required = false)
  private final List<GlobalBindingContextProvider> globalBindingContextProviders = emptyList();

  @Override
  public ExtendedExpressionManager getObject() throws Exception {
    DefaultExpressionManager delegate = createBaseObject();
    ExtendedExpressionLanguageAdaptor expressionLanguage = createExpressionLanguage();
    populateBindings(expressionLanguage);
    delegate.setExpressionLanguage(expressionLanguage);

    muleContext.getInjector().inject(delegate);
    return (ExtendedExpressionManager) createClassLoaderInjectorInvocationHandler(delegate,
                                                                                  muleContext.getExecutionClassLoader());
  }

  protected void populateBindings(ExtendedExpressionLanguageAdaptor expressionLanguage) {
    BindingContext.Builder contextBuilder = BindingContext.builder();

    globalBindingContextProviders.stream()
        .map(GlobalBindingContextProvider::getBindingContext)
        .forEach(contextBuilder::addAll);

    expressionLanguage.addGlobalBindings(contextBuilder instanceof DefaultBindingContextBuilder
        ? ((DefaultBindingContextBuilder) contextBuilder).flattenAndBuild()
        : contextBuilder.build());
  }

  protected DefaultExpressionManager createBaseObject() {
    return new DefaultExpressionManager();
  }

  private ExtendedExpressionLanguageAdaptor createExpressionLanguage() {
    if (dwExpressionLanguage == null) {
      throw new IllegalStateException("No expression language installed");
    }
    return dwExpressionLanguage;
  }

  @Override
  public Class<?> getObjectType() {
    return ExtendedExpressionManager.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
