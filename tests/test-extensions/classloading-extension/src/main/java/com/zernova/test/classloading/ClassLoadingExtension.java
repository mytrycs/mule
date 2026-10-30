/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.classloading;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Export;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.test.classloading.api.ClassLoadingHelper;
import com.zernova.test.classloading.api.validation.ClassLoadingValidationsProvider;

@Extension(name = "ClassLoading")
@JavaVersionSupport({JAVA_21, JAVA_17})
@Xml(prefix = "classloading")
@ConnectionProviders({CLPoolingConnectionProvider.class, CLCachedConnectionProvider.class, CLNoneConnectionProvider.class})
@Configurations({CLConfiguration.class, CLInvalidConfiguration.class})
@Export(classes = {ClassLoadingHelper.class, ClassLoadingValidationsProvider.class},
    resources = {"META-INF/services/com.zernova.runtime.ast.api.validation.ValidationsProvider"})
public class ClassLoadingExtension {

}
