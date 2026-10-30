/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.el;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.el.DefaultExpressionLanguageFactoryService;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.el.ExtendedExpressionLanguageAdaptor;
import com.zernova.runtime.core.internal.el.dataweave.DataWeaveExpressionLanguageAdaptor;

import jakarta.inject.Inject;

import org.springframework.beans.factory.FactoryBean;

public class DataWeaveExtendedExpressionLanguageAdaptorFactoryBean implements FactoryBean<ExtendedExpressionLanguageAdaptor> {

  @Inject
  private DefaultExpressionLanguageFactoryService dwExpressionLanguageFactory;

  @Inject
  private FeatureFlaggingService featureFlaggingService;

  @Inject
  private ZernovaContext muleContext;

  @Inject
  private ArtifactEncoding artifactEncoding;

  @Inject
  private Registry registry;

  @Override
  public ExtendedExpressionLanguageAdaptor getObject() throws Exception {
    return new DataWeaveExpressionLanguageAdaptor(muleContext, registry,
                                                  muleContext.getConfiguration(), artifactEncoding,
                                                  dwExpressionLanguageFactory, featureFlaggingService);
  }

  @Override
  public Class<?> getObjectType() {
    return ExtendedExpressionLanguageAdaptor.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}
