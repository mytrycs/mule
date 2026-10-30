/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.javaxinject;

import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.sdk.api.annotation.Operations;
import com.zernova.sdk.api.annotation.Sources;
import com.zernova.sdk.api.annotation.connectivity.ConnectionProviders;

import javax.inject.Inject;

@Configuration
@ConnectionProviders(JavaxInjectCompatibilityTestConnectionProvider.class)
@Sources(JavaxInjectCompatibilityTestSource.class)
@Operations(JavaxInjectCompatibilityTestOperations.class)
public class JavaxInjectCompatibilityTestConfiguration {

  @Inject
  private ArtifactEncoding encoding;

  public ArtifactEncoding getEncoding() {
    return encoding;
  }
}
