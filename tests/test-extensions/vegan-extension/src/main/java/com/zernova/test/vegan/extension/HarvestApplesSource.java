/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import static java.lang.String.format;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.execution.OnSuccess;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataKeyId;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.ConfigOverride;
import com.zernova.runtime.extension.api.annotation.param.NullSafe;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.tck.testmodels.fruit.Apple;

import java.util.Arrays;
import java.util.List;

@Alias("harvest-apples")
@MetadataScope(keysResolver = HarvestAppleKeyResolver.class,
    outputResolver = HarvestAppleKeyResolver.class)
public class HarvestApplesSource extends Source<Apple, HarvestApplesAttributes> {

  @Config
  private AppleConfig appleConfig;

  @MetadataKeyId
  @Parameter
  @Optional
  private String key;

  @Parameter
  @ConfigOverride
  private List<String> mainProducers;

  @ParameterGroup(name = "Sample Input Group")
  private HarvestInputGroup sampleInput;

  @Parameter
  @Optional
  private String shouldNotOverride;

  @Parameter
  @Optional
  private String flowName;

  @com.zernova.sdk.api.annotation.param.ParameterGroup(name = "As Group Inline", showInDsl = true)
  private GroupedFood inlineGroupedFood;

  @Parameter
  @Optional
  @NullSafe
  private GroupedFood pojoGroupedFood;

  @Override
  public void onStart(SourceCallback<Apple, HarvestApplesAttributes> sourceCallback) throws ZernovaException {
    if (inlineGroupedFood.getFood() == null || pojoGroupedFood.getFood() == null) {
      throw new IllegalArgumentException(
                                         format("Got a null in food groups: %s", inlineGroupedFood.getFood(),
                                                pojoGroupedFood.getFood()));
    }

    sourceCallback.handle(Result.<Apple, HarvestApplesAttributes>builder()
        .output(null).attributes(null).build());
  }

  @OnSuccess
  public void onSuccess(@ParameterGroup(name = "Response", showInDsl = true) SuccessResponse response) {
    appleConfig.getResults()
        .put(flowName, Arrays.asList(mainProducers, sampleInput.getSample(), shouldNotOverride, response.getTimeToPeel()));
  }

  @Override
  public void onStop() {}
}
