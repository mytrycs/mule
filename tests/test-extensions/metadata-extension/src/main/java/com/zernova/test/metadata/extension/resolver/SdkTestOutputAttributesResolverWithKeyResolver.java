/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.resolver;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;

import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.builder.ObjectTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.sdk.api.metadata.MetadataContext;
import com.zernova.sdk.api.metadata.resolving.AttributesTypeResolver;
import com.zernova.sdk.api.metadata.resolving.OutputTypeResolver;

import java.util.Set;

public class SdkTestOutputAttributesResolverWithKeyResolver
    implements TypeKeysResolver, OutputTypeResolver<String>, AttributesTypeResolver<String> {

  @Override
  public Set<MetadataKey> getKeys(com.zernova.runtime.api.metadata.MetadataContext context)
      throws MetadataResolvingException, ConnectionException {
    return TestMetadataResolverUtils.getKeys(context);
  }

  @Override
  public MetadataType getOutputType(MetadataContext context, String key) throws MetadataResolvingException {
    return TestMetadataResolverUtils.getMetadata(key);
  }

  @Override
  public MetadataType getAttributesType(MetadataContext context, String key)
      throws MetadataResolvingException, ConnectionException {
    ObjectTypeBuilder builder = BaseTypeBuilder.create(JAVA).objectType();
    builder.openWith().stringType();
    return builder.build();
  }

  @Override
  public String getCategoryName() {
    return "MetadataExtensionResolver";
  }

  @Override
  public String getResolverName() {
    return "SdkTestOutputAttributesResolverWithKeyResolver";
  }
}
