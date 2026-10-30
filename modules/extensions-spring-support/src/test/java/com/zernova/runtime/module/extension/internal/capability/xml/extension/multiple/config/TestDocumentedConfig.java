/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.capability.xml.extension.multiple.config;

import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;

/**
 * This is some Config documentation.
 */
@Configuration(name = "config")
@Operations({TestDocumentedExtensionOperations.class})
@ConnectionProviders({TestDocumentedProvider.class, TestAnotherDocumentedProvider.class})
public class TestDocumentedConfig {

  /**
   * Config parameter
   */
  @Parameter
  private String configParameter;

  /**
   * Config {@link Parameter} with an {@link Optional} value
   */
  @Parameter
  @Optional
  private String configParameterWithComplexJavadoc;

  @ParameterGroup(name = "group")
  private TestDocumentedParameterGroup group;
}
