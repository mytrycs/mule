/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.resolver;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.test.metadata.extension.MetadataConnection.PERSON;
import static com.zernova.test.metadata.extension.resolver.TestMetadataResolverUtils.getMetadata;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;

public class TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam
    implements InputTypeResolver<String>, OutputTypeResolver<String> {

  private static final String KEY_SHOULD_BE_EMPTY = "Metadata resolvers without Key Resolver should get a NullMetadataKey as Key";

  @Override
  public String getResolverName() {
    return "TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam";
  }

  @Override
  public MetadataType getInputMetadata(MetadataContext context, String key) throws MetadataResolvingException {
    checkArgument(isBlank(key), KEY_SHOULD_BE_EMPTY);
    return getMetadata(PERSON);
  }

  @Override
  public MetadataType getOutputType(MetadataContext context, String key) throws MetadataResolvingException {
    checkArgument(isBlank(key), KEY_SHOULD_BE_EMPTY);
    return getMetadata(PERSON);
  }

  @Override
  public String getCategoryName() {
    return "MetadataExtensionResolver";
  }
}
