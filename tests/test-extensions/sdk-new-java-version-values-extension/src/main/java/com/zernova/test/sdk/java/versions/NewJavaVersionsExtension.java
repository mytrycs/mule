/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.sdk.java.versions;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_1000;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.sdk.api.annotation.JavaVersionSupport;

@Extension(name = "New Java versions")
@JavaVersionSupport({JAVA_17, JAVA_21, JAVA_1000})
@Xml(prefix = "new-java-versions")
public class NewJavaVersionsExtension {
}
