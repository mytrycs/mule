/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension;

import static com.zernova.test.metadata.extension.MetadataConnection.PERSON;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataKeyId;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.tck.message.StringAttributes;
import com.zernova.test.metadata.extension.resolver.TestInputOutputSourceResolverWithKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestInputAndOutputWithAttributesResolverWithKeyResolver;

import java.util.Map;


@MetadataScope(keysResolver = TestInputOutputSourceResolverWithKeyResolver.class,
    outputResolver = TestInputAndOutputWithAttributesResolverWithKeyResolver.class)
public class MetadataSource extends Source<Map<String, Object>, StringAttributes> {

  public static boolean STARTED = false;

  @MetadataKeyId
  @Parameter
  public String type;

  @Connection
  private ConnectionProvider<MetadataConnection> connection;

  @Override
  public void onStart(SourceCallback<Map<String, Object>, StringAttributes> sourceCallback) throws ZernovaException {
    STARTED = true;
    if (!type.equals(PERSON)) {
      throw new RuntimeException(String.format("Invalid MetadataKey with value [%s], the key should be [%s]", type, PERSON));
    }
  }

  @Override
  public void onStop() {
    STARTED = false;
  }
}
