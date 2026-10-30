/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension;

import static com.zernova.test.metadata.extension.resolver.TestMultiLevelKeyResolver.AMERICA;
import static com.zernova.test.metadata.extension.resolver.TestMultiLevelKeyResolver.ARGENTINA;
import static com.zernova.test.metadata.extension.resolver.TestMultiLevelKeyResolver.BUENOS_AIRES;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataKeyId;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.tck.message.StringAttributes;
import com.zernova.test.metadata.extension.resolver.TestMultiLevelKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestPartialMultiLevelKeyResolver;

import java.util.Map;

@MetadataScope(keysResolver = TestMultiLevelKeyResolver.class,
    outputResolver = TestPartialMultiLevelKeyResolver.class)
public class MetadataSourceWithMultilevel extends Source<Map<String, Object>, StringAttributes> {

  private static final String ERROR_MESSAGE = "LocationKey field was not injected properly";

  @MetadataKeyId
  @com.zernova.sdk.api.annotation.param.ParameterGroup(name = "Location")
  public LocationKey key;

  @Override
  public void onStart(SourceCallback<Map<String, Object>, StringAttributes> sourceCallback) throws ZernovaException {
    boolean injectedProperly = key != null && key.getCity().equals(BUENOS_AIRES) && key.getCountry().equals(ARGENTINA)
        && key.getContinent().equals(AMERICA);

    if (!injectedProperly) {
      throw new RuntimeException(ERROR_MESSAGE);
    }
  }

  @Override
  public void onStop() {

  }
}
