/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.NullSafe;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.annotation.param.display.Placement;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class SpreadVeganismOperation implements Initialisable, Startable, Stoppable {

  public static final String ARGUMENTS_TAB = "Arguments";

  private ScheduledExecutorService longTaskExecutor;

  @Override
  public void initialise() throws InitialisationException {
    if (eloquenceLevel == null || eloquenceLevel <= 0) {
      eloquenceLevel = 1;
    }
  }

  @Override
  public void start() throws ZernovaException {
    longTaskExecutor = newScheduledThreadPool(1);
  }

  @Override
  public void stop() throws ZernovaException {
    longTaskExecutor.shutdownNow();
  }

  @Parameter
  @Optional(defaultValue = "2")
  private Integer eloquenceLevel;

  @MediaType(TEXT_PLAIN)
  public String spreadTheWord(String theWord, @Config Object config) {
    StringBuilder message = new StringBuilder();
    for (int i = 0; i < eloquenceLevel; i++) {
      message.append(theWord);
    }

    return message.toString();
  }

  public VeganPolicy applyPolicy(@Optional @NullSafe VeganPolicy policy) {
    return policy;
  }

  @OutputResolver(output = FruitMetadataResolver.class)
  public FarmedFood getProduction(@Optional @NullSafe(defaultImplementingType = HealthyFood.class) FarmedFood food) {
    return food;
  }

  @OutputResolver(output = FruitMetadataResolver.class)
  public FarmedFood getHealthyFood(HealthyFood healthyFood) {
    return healthyFood;
  }

  @MediaType(TEXT_PLAIN)
  public void longDigest(CompletionCallback<String, Object> callback) {
    longTaskExecutor
        .schedule(() -> callback.success(Result.<String, Object>builder().output("Hello!").build()), 10, SECONDS);
  }

  public Map<String, String> addVeganProductsDescriptions(@Optional @NullSafe Map<String, String> productDescription) {
    return productDescription;
  }

  public List<String> registerVeganProducts(@Optional @NullSafe List<String> products) {
    return products;
  }

  public void convinceAnimalKiller(@com.zernova.sdk.api.annotation.param.ParameterGroup(name = "arguments") @Placement(
      tab = ARGUMENTS_TAB) VeganArguments arguments) {

  }

}
