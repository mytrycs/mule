/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.ANY;

import com.zernova.runtime.extension.api.annotation.metadata.MetadataKeyId;
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.tck.testmodels.fruit.Apple;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EatAppleOperation {

  @OutputResolver(output = AppleTypesResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object eatApple(@com.zernova.sdk.api.annotation.param.Connection Apple apple,
                         @com.zernova.sdk.api.annotation.param.Config AppleConfig config, @MetadataKeyId @Optional String key) {
    apple.bite();
    return apple;
  }

  public List<String> getAllApples(@Content Map<String, Apple> apples) {
    return apples.keySet().stream().collect(Collectors.toList());
  }
}
