/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.oauth.metadata;

import static java.util.Arrays.asList;
import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.runtime.api.metadata.MetadataKeyBuilder.newKey;

import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.AttributesTypeResolver;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.QueryEntityResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.extension.api.connectivity.oauth.AccessTokenExpiredException;
import com.zernova.test.oauth.TestOAuthConnection;

import java.util.HashSet;
import java.util.Set;

public class RefreshedOAuthMetadataResolver implements OutputTypeResolver<String>, AttributesTypeResolver<String>,
    InputTypeResolver<String>, TypeKeysResolver, QueryEntityResolver {

  @Override
  public Set<MetadataKey> getKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException {
    checkIfTokenIsRefreshed(context);
    return new HashSet(asList(newKey("anyKey").build()));
  }

  @Override
  public String getResolverName() {
    return "Metadata resolver";
  }

  @Override
  public Set<MetadataKey> getEntityKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException {
    checkIfTokenIsRefreshed(context);
    return new HashSet(asList(newKey("anyKey").build()));
  }

  @Override
  public MetadataType getEntityMetadata(MetadataContext context, String key)
      throws MetadataResolvingException, ConnectionException {
    checkIfTokenIsRefreshed(context);
    return BaseTypeBuilder.create(JAVA).stringType().build();
  }

  @Override
  public MetadataType getInputMetadata(MetadataContext context, String key)
      throws MetadataResolvingException, ConnectionException {
    checkIfTokenIsRefreshed(context);
    return BaseTypeBuilder.create(JAVA).stringType().build();
  }

  @Override
  public MetadataType getAttributesType(MetadataContext context, String key)
      throws MetadataResolvingException, ConnectionException {
    checkIfTokenIsRefreshed(context);
    return BaseTypeBuilder.create(JAVA).stringType().build();
  }

  @Override
  public MetadataType getOutputType(MetadataContext context, String key) throws MetadataResolvingException, ConnectionException {
    checkIfTokenIsRefreshed(context);
    return BaseTypeBuilder.create(JAVA).stringType().build();
  }

  private void checkIfTokenIsRefreshed(MetadataContext context) throws ConnectionException {
    TestOAuthConnection testOAuthConnection = (TestOAuthConnection) context.getConnection().get();
    if (!testOAuthConnection.getState().getState().getAccessToken().contains("refreshed")) {
      throw new AccessTokenExpiredException();
    }
  }

  @Override
  public String getCategoryName() {
    return "OAuth Category";
  }

}
