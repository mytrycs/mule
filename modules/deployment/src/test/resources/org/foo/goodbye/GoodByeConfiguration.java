/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.goodbye;

import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.client.ExtensionsClient;

import jakarta.inject.Inject;

@Configuration(name = "config")
public class GoodByeConfiguration {

  @Inject
  private ExtensionsClient extensionsClient;

}