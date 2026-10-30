/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.tck.testmodels.fruit.Fruit;

@MetadataScope(outputResolver = FruitMetadataResolver.class)
public class PaulMcCartneySource extends Source<Fruit, Object> {

  @Config
  private Object config;

  @Override
  public void onStart(SourceCallback<Fruit, Object> sourceCallback) throws ZernovaException {

  }

  @Override
  public void onStop() {

  }
}
