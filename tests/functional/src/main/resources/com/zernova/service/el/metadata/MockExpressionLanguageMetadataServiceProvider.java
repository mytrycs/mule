/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.service.el.metadata;

import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.api.service.ServiceDefinition;
import com.zernova.runtime.api.service.ServiceProvider;

public class MockExpressionLanguageMetadataServiceProvider implements ServiceProvider {

  private final ExpressionLanguageMetadataService service = new MockExpressionLanguageMetadataService();

  @Override
  public ServiceDefinition getServiceDefinition() {
    return new ServiceDefinition(ExpressionLanguageMetadataService.class, service);
  }
}
