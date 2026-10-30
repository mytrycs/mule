/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.function.extension;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.extension.api.annotation.ExpressionFunctions;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Import;
import com.zernova.runtime.extension.api.annotation.ImportedTypes;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.test.heisenberg.extension.model.KnockeableDoor;



@Extension(name = "Test Functions")
@JavaVersionSupport({JAVA_21, JAVA_17})
@ExpressionFunctions(GlobalWeaveFunction.class)
@Operations(WeaveTestUtilsOperations.class)
@Xml(prefix = "fn")
@ImportedTypes(@Import(type = KnockeableDoor.class))
public class WeaveFunctionExtension {

}
