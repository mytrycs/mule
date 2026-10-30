/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.oauth;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Import;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.test.values.extension.MyPojo;

@Extension(name = TestOAuthExtension.TEST_OAUTH_EXTENSION_NAME)
@JavaVersionSupport({JAVA_21, JAVA_17})
@Configurations({AuthCodeConfig.class, ClientCredentialsConfig.class, MixedConfig.class, WithPooledProviderConfig.class,
    WithValidationThatNeedsRefreshConfig.class})
@Xml(prefix = "test-oauth")
@Import(type = MyPojo.class)
public class TestOAuthExtension {

  public static final String TEST_OAUTH_EXTENSION_NAME = "Test OAuth Extension";

}
