/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import com.zernova.runtime.core.privileged.exception.TemplateOnErrorHandler;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;

/**
 * An {@link com.zernova.runtime.dsl.api.component.ObjectFactory} which generates {@link TemplateOnErrorHandler} based on existing
 * ones.
 *
 * @since 4.1.0
 */
public class OnErrorFactoryBean extends AbstractComponentFactory<TemplateOnErrorHandler> {

  private final TemplateOnErrorHandler onErrorHandler;

  public OnErrorFactoryBean(TemplateOnErrorHandler onErrorHandler) {
    this.onErrorHandler = onErrorHandler;
  }

  @Override
  public TemplateOnErrorHandler doGetObject() throws Exception {
    return onErrorHandler;
  }

}
