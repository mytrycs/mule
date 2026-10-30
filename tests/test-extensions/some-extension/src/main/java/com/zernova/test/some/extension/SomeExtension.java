/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.some.extension;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Export;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.sdk.api.annotation.error.ErrorTypes;
import com.zernova.test.heisenberg.extension.HeisenbergErrors;

@Extension(name = "SomeExtension")
@JavaVersionSupport({JAVA_21, JAVA_17})
@Configurations({ParameterGroupConfig.class, ParameterGroupDslConfig.class})
@ConnectionProviders(ExtConnProvider.class)
@ErrorTypes(HeisenbergErrors.class)
@Export(classes = CustomConnectionException.class)
@Sources({SomeEmittingSource.class, AnotherEmittingSource.class, YetAnotherEmittingSource.class,
    ParameterEmittingSource.class, ExclusiveOptionalsEmittingSource.class, AnotherExclusiveOptionalsEmittingSource.class})
@Xml(namespace = "http://www.mulesoft.org/schema/mule/some", prefix = "some")
public class SomeExtension {
}
