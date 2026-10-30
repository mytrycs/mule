/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import com.zernova.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.tck.testmodels.fruit.Banana;

public class EatBananaOperation {

  public Banana eatBanana(@Connection Banana banana, @Config BananaConfig config) {
    banana.bite();
    return banana;
  }

  public Banana eatPealed(@ParameterDsl(allowInlineDefinition = false, allowReferences = false) Banana attributeOnlyBanana,
                          @Config BananaConfig config) {
    return eatBanana(attributeOnlyBanana, config);
  }

  public BananaConfig getConfig(@Config BananaConfig config) {
    return config;
  }
}
