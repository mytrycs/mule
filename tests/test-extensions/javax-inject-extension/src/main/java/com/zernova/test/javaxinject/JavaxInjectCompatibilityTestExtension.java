/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.javaxinject;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.sdk.api.annotation.Configurations;
import com.zernova.sdk.api.annotation.ExpressionFunctions;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.sdk.api.annotation.dsl.xml.Xml;

import javax.inject.Inject;

@Extension(name = JavaxInjectCompatibilityTestExtension.JAVAX_INJECT_COMPATIBILITY_TEST_EXTENSION)
@Xml(prefix = "javax-inject",
    namespace = "http://www.mulesoft.org/schema/mule/javax-inject")
@JavaVersionSupport({JAVA_21, JAVA_17})
@Configurations(JavaxInjectCompatibilityTestConfiguration.class)
@ExpressionFunctions(JavaxInjectCompatibilityTestFunction.class)
public class JavaxInjectCompatibilityTestExtension {

  public static final String JAVAX_INJECT_COMPATIBILITY_TEST_EXTENSION = "Javax Inject compatibility Test Extension";

  @Inject
  private ArtifactEncoding encoding;

  public ArtifactEncoding getEncoding() {
    return encoding;
  }
}
