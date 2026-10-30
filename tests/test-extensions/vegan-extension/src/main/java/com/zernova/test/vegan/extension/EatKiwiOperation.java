/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.tck.testmodels.fruit.Kiwi;
import com.zernova.tck.testmodels.fruit.WaterMelon;

public class EatKiwiOperation {

  public Kiwi eatKiwi(@com.zernova.sdk.api.annotation.param.Connection Kiwi kiwi, @Config KiwiConfig config) {
    kiwi.bite();
    return kiwi;
  }

  public WaterMelon eatWatermelon(WaterMelon fruit) {
    throw new IllegalArgumentException("Unlike popular belief, a Kiwi is not a small water melon");
  }
}
