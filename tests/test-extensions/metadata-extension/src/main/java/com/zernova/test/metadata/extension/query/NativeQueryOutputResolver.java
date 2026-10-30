/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension.query;

import com.zernova.metadata.api.builder.ObjectTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;

public class NativeQueryOutputResolver implements OutputTypeResolver<String> {

  public static final String NATIVE_QUERY = "SELECT FIELDS: field-id FROM TYPE: Circle DO WHERE field-diameter < 18";
  public static final String QUERY_RESOLVER_CATEGORY = "QueryResolverCategory";

  @Override
  public String getResolverName() {
    return "NativeQueryResolver";
  }

  @Override
  public String getCategoryName() {
    return QUERY_RESOLVER_CATEGORY;
  }

  @Override
  public MetadataType getOutputType(MetadataContext context, String key)
      throws MetadataResolvingException, ConnectionException {

    if (!key.equals(NATIVE_QUERY)) {
      throw new IllegalArgumentException("Native Query Key was not the expected one");
    }

    final ObjectTypeBuilder objectType = context.getTypeBuilder().objectType();
    objectType.addField().key("id").value().numberType();
    return objectType.build();
  }
}
