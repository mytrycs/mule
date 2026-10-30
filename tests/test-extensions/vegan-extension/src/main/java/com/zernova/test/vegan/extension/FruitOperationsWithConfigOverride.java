/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.ConfigOverride;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.test.vegan.extension.EasyToEatConfig.Time;

import java.util.List;

public class FruitOperationsWithConfigOverride {

  public Integer getTimeToPeel(@Config EasyToEatConfig config,
                               @ConfigOverride Integer timeToPeel) {
    return timeToPeel;
  }

  public long getTimeToPeelLong(@com.zernova.sdk.api.annotation.param.Config EasyToEatConfig config,
                                @ConfigOverride long timeToPeel) {
    return timeToPeel;
  }

  public Time getTimeToPeelEnum(@Config EasyToEatConfig config,
                                @ConfigOverride Time timeToPeelEnum) {
    return timeToPeelEnum;
  }

  public List<String> getProducers(@com.zernova.sdk.api.annotation.param.Config EasyToEatConfig config,
                                   @ConfigOverride List<String> mainProducers) {
    return mainProducers;
  }

  public HealthyFood getFruitSample(@Config EasyToEatConfig config,
                                    @ConfigOverride HealthyFood sample) {
    return sample;
  }

  @MediaType(TEXT_PLAIN)
  public String getNotOverridenParameter(@Config EasyToEatConfig config,
                                         @Optional String shouldNotOverride) {
    return shouldNotOverride;
  }

}
