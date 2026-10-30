/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.customos.internal;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.test.customos.internal.connection.MyOSConnectionProvider;
import com.zernova.test.customos.internal.operation.MyOSOperations;

import java.util.HashMap;
import java.util.Map;

@Extension(name = "MyOS")
@JavaVersionSupport({JAVA_21, JAVA_17})
@Operations(MyOSOperations.class)
@ConnectionProviders(MyOSConnectionProvider.class)
@Xml(prefix = "custom-os")
public class MyOSConnector {

  public static Map<String, TypedValue<String>> VALUES = new HashMap<>();
}
