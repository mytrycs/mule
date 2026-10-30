/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;
import static com.zernova.test.marvel.MarvelExtension.MARVEL_EXTENSION;

import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Export;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.error.ErrorTypes;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.test.marvel.drstrange.DrStrange;
import com.zernova.test.marvel.drstrange.DrStrangeErrorTypeDefinition;
import com.zernova.test.marvel.drstrange.DrStrangeTypeWithCustomStereotype;
import com.zernova.test.marvel.ironman.IronMan;
import com.zernova.test.marvel.xmen.XMen;

@Extension(name = MARVEL_EXTENSION)
@JavaVersionSupport({JAVA_21, JAVA_17})
@Configurations({IronMan.class, DrStrange.class, XMen.class})
@ErrorTypes(DrStrangeErrorTypeDefinition.class)
@Export(classes = {IronMan.class, DrStrangeTypeWithCustomStereotype.class})
public class MarvelExtension {

  public static final String MARVEL_EXTENSION = "Marvel";
}
