/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.semantic.extension;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.test.semantic.extension.connection.ApiKeySemanticConnectionProvider;
import com.zernova.test.semantic.extension.connection.BasicAuthSemanticConnectionProvider;
import com.zernova.test.semantic.extension.connection.ClientSecretSemanticConnectionProvider;
import com.zernova.test.semantic.extension.connection.CustomAuthSemanticConnectionProvider;
import com.zernova.test.semantic.extension.connection.DigestSemanticConnectionProvider;
import com.zernova.test.semantic.extension.connection.KerberosSemanticConnectionProvider;
import com.zernova.test.semantic.extension.connection.UnsecuredSemanticConnectionProvider;

@Extension(name = "Semantic Terms")
@JavaVersionSupport({JAVA_21, JAVA_17})
@Operations(SemanticTermsOperations.class)
@ConnectionProviders({BasicAuthSemanticConnectionProvider.class, ApiKeySemanticConnectionProvider.class,
    ClientSecretSemanticConnectionProvider.class, CustomAuthSemanticConnectionProvider.class,
    DigestSemanticConnectionProvider.class, KerberosSemanticConnectionProvider.class,
    UnsecuredSemanticConnectionProvider.class})
@Sources(SemanticTermsSource.class)
public class SemanticTermsExtension {

}
