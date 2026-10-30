/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.NullSafe;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.tck.testmodels.fruit.Fruit;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class VeganFidelityOperation implements Initialisable {

  @ParameterGroup(name = "Arguments")
  private VeganArguments arguments;

  @Override
  public void initialise() throws InitialisationException {
    if (arguments == null) {
      throw new IllegalStateException("Forgot why I was began");
    }
  }

  @MediaType(TEXT_PLAIN)
  public String tryEat(@ParameterDsl(allowReferences = false) Object food,
                       @Optional @ParameterDsl(allowInlineDefinition = false) Map<String, Integer> menu) {
    if (food instanceof Fruit) {
      return "tasty " + food.getClass().getSimpleName();
    }

    throw new IllegalArgumentException("I SHALL NEVER EAT " + food.toString() + " because " + arguments.toString());
  }

  public List<Map<String, String>> tryToEatThisListOfMaps(@Optional @NullSafe @Content List<Map<String, String>> foods) {
    if (foods != null) {
      return foods;
    }
    throw new IllegalArgumentException("I CAN'T EAT THAT TYPE, IS IMPOSSIBLE!");
  }

  @OutputResolver(output = FruitMetadataResolver.class)
  public List<FarmedFood> consumingGroupedFood(@ParameterGroup(name = "As Group") GroupedFood groupedFood,
                                               @Optional @NullSafe GroupedFood pojoGroupedFood) {
    return Arrays.asList(groupedFood.getFood(), pojoGroupedFood.getFood());
  }

}
