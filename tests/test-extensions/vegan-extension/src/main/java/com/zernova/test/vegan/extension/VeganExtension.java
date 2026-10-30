/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;


import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.SubTypeMapping;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.sdk.api.annotation.JavaVersionSupport;

@Extension(name = VeganExtension.VEGAN)
@JavaVersionSupport({JAVA_21, JAVA_17})
@Configurations({AppleConfig.class, BananaConfig.class, KiwiConfig.class, PeachConfig.class, PearConfig.class, GrapeConfig.class})
@Operations(VeganFidelityOperation.class)
@SubTypeMapping(baseType = FarmedFood.class, subTypes = {RottenFood.class, HealthyFood.class})
@MetadataScope(keysResolver = AppleTypesResolver.class)
public class VeganExtension {

  public static final String VEGAN = "vegan";
  public static final String APPLE = "apple-config";
  public static final String BANANA = "banana-config";
  public static final String KIWI = "kiwi-config";
  public static final String PEACH = "peach-config";

}

