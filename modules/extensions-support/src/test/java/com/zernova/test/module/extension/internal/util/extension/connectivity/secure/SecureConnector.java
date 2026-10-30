/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.internal.util.extension.connectivity.secure;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.param.display.Password;
import com.zernova.runtime.extension.api.annotation.param.display.Text;

@Extension(name = "secure")
@Operations(SecureOperations.class)
@ConnectionProviders(SecureConnectionProvider.class)
@Xml(namespace = "https://schemas.zernova.dev/secure", prefix = "secure")
public class SecureConnector {

  @Parameter
  @Text
  private String plainStringField;

  @Parameter
  @Password
  private String password;


}
