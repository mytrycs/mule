/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;


import static com.zernova.test.vegan.extension.VeganExtension.APPLE;
import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.deprecated.Deprecated;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.stereotype.Stereotype;
import com.zernova.test.vegan.extension.stereotype.AppleStereotype;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration(name = APPLE)
@Operations({EatAppleOperation.class, SpreadVeganismOperation.class, VeganFidelityOperation.class,
    FruitOperationsWithConfigOverride.class})
@Sources({HarvestApplesSource.class, PaulMcCartneySource.class})
@ConnectionProviders(VeganAppleConnectionProvider.class)
@Stereotype(AppleStereotype.class)
@Deprecated(message = "This configuration overlaps with the BananaConfig, use that one instead.", since = "1.2.0")
public class AppleConfig extends EasyToEatConfig {

  @Parameter
  private VeganCookBook cookBook;

  private Map<String, List<Object>> results = new HashMap<>();

  public VeganCookBook getCookBook() {
    return cookBook;
  }

  public Map<String, List<Object>> getResults() {
    return results;
  }
}
