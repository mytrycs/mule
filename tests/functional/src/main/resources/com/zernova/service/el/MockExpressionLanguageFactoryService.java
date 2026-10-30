/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.service.el;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import com.zernova.runtime.api.el.DefaultExpressionLanguageFactoryService;
import com.zernova.runtime.api.el.ExpressionLanguage;
import com.zernova.runtime.api.el.ExpressionLanguageConfiguration;

public class MockExpressionLanguageFactoryService implements DefaultExpressionLanguageFactoryService {

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public ExpressionLanguage create() {
    return new MockExpressionLanguage();
  }

  @Override
  public ExpressionLanguage create(ExpressionLanguageConfiguration configuration) {
    return create();
  }
}
