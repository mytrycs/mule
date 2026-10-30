/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.client;

import com.zernova.sdk.api.annotation.Extension;
import com.zernova.sdk.api.annotation.Operations;

@Extension(name = DefaultExtensionsClientTestExtension.DEFAULT_EXTENSIONS_CLIENT_TEST_EXTENSION_NAME)
@Operations(DefaultExtensionsClientTestOperations.class)
public class DefaultExtensionsClientTestExtension {

  public static final String DEFAULT_EXTENSIONS_CLIENT_TEST_EXTENSION_NAME = "Default Extensions Client Test";
}
