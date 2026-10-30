/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.config;

import static java.util.ServiceLoader.load;

import com.zernova.runtime.api.el.DefaultExpressionLanguageFactoryService;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.weave.v2.el.provider.WeaveDefaultExpressionLanguageFactoryService;
import com.zernova.weave.v2.el.metadata.WeaveExpressionLanguageMetadataServiceImpl;

import java.util.Iterator;

/**
 * Provides a way to override how the {@link DefaultExpressionLanguageFactoryService} for DataWeave to use in tests is obtained.
 *
 * @since 4.5
 */
public interface WeaveExpressionLanguageFactoryServiceProvider {

  public static DefaultExpressionLanguageFactoryService provideDefaultExpressionLanguageFactoryService() {
    final Iterator<WeaveExpressionLanguageFactoryServiceProvider> iterator =
        load(WeaveExpressionLanguageFactoryServiceProvider.class,
             WeaveExpressionLanguageFactoryServiceProvider.class.getClassLoader()).iterator();

    if (iterator.hasNext()) {
      return iterator.next().createDefaultExpressionLanguageFactoryService();
    } else {
      return new WeaveDefaultExpressionLanguageFactoryService(null);
    }
  }

  public static ExpressionLanguageMetadataService provideExpressionLanguageMetadataService() {
    final Iterator<WeaveExpressionLanguageFactoryServiceProvider> iterator =
        load(WeaveExpressionLanguageFactoryServiceProvider.class,
             WeaveExpressionLanguageFactoryServiceProvider.class.getClassLoader()).iterator();

    if (iterator.hasNext()) {
      return iterator.next().createExpressionLanguageMetadataService();
    } else {
      return new WeaveExpressionLanguageMetadataServiceImpl();
    }
  }

  DefaultExpressionLanguageFactoryService createDefaultExpressionLanguageFactoryService();

  ExpressionLanguageMetadataService createExpressionLanguageMetadataService();

}
