/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.metadata;

import static com.zernova.runtime.api.component.location.Location.builder;
import static com.zernova.runtime.api.metadata.MetadataKeyBuilder.newKey;
import static com.zernova.tck.junit4.matcher.MetadataKeyMatcher.metadataKeyWithId;
import static com.zernova.tck.junit4.matcher.metadata.MetadataKeyResultSuccessMatcher.isSuccess;
import static com.zernova.test.metadata.extension.query.MetadataExtensionEntityResolver.CIRCLE;
import static com.zernova.test.metadata.extension.query.MetadataExtensionEntityResolver.SQUARE;
import static com.zernova.test.metadata.extension.query.NativeQueryOutputResolver.NATIVE_QUERY;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.toMetadataType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.zernova.metadata.api.model.ArrayType;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.NumberType;
import com.zernova.metadata.api.model.ObjectFieldType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataKeysContainer;
import com.zernova.runtime.api.metadata.descriptor.ComponentMetadataDescriptor;
import com.zernova.runtime.api.metadata.descriptor.TypeMetadataDescriptor;
import com.zernova.runtime.api.metadata.resolving.MetadataResult;
import com.zernova.test.metadata.extension.model.shapes.Circle;

import java.util.Set;

import org.junit.Test;

public class QueryMetadataTestCase extends AbstractMetadataOperationTestCase {

  private static final MetadataKey CIRCLE_METADATA_KEY = newKey(CIRCLE).build();
  private static final Location QUERY_LOCATION =
      builder().globalName(QUERY_LIST_FLOW).addProcessorsPart().addIndexPart(0).build();

  public QueryMetadataTestCase(ResolutionType resolutionType) {
    super(resolutionType);
  }

  @Override
  protected String getConfigFile() {
    return METADATA_TEST;
  }

  @Test
  public void getEntityKeys() throws Exception {
    MetadataResult<MetadataKeysContainer> metadataKeysResult = metadataService.getEntityKeys(QUERY_LOCATION);
    assertThat(metadataKeysResult, isSuccess());
    MetadataKeysContainer container = metadataKeysResult.get();
    Set<MetadataKey> metadataKeys = container.getKeys(container.getCategories().iterator().next()).get();
    assertThat(metadataKeys.size(), is(2));
    assertThat(metadataKeys, hasItems(metadataKeyWithId(CIRCLE), metadataKeyWithId(SQUARE)));
  }

  @Test
  public void getEntityMetadata() throws Exception {
    MetadataResult<TypeMetadataDescriptor> entityMetadata =
        metadataService.getEntityMetadata(QUERY_LOCATION, CIRCLE_METADATA_KEY);
    assertThat(entityMetadata, isSuccess());
    TypeMetadataDescriptor descriptor = entityMetadata.get();
    assertThat(descriptor.getType(), is(toMetadataType(Circle.class)));
  }

  @Test
  public void getDsqlQueryAutomaticGeneratedOutputMetadata() throws Exception {
    location = QUERY_LOCATION;
    MetadataKey dsqlKey = newKey(DSQL_QUERY).build();
    MetadataResult<ComponentMetadataDescriptor<OperationModel>> entityMetadata = getComponentDynamicMetadata(dsqlKey);
    assertThat(entityMetadata, isSuccess());

    MetadataType generatedType = entityMetadata.get().getModel().getOutput().getType();
    assertThat(generatedType, is(instanceOf(ArrayType.class)));
    assertCircleType((ObjectType) ((ArrayType) generatedType).getType());
  }

  @Test
  public void getNativeQueryOutputArrayMetadata() throws Exception {
    location = builder().globalName(NATIVE_QUERY_LIST_FLOW).addProcessorsPart().addIndexPart(0).build();
    MetadataKey nativeKey = newKey(NATIVE_QUERY).build();
    MetadataResult<ComponentMetadataDescriptor<OperationModel>> entityMetadata = getComponentDynamicMetadata(nativeKey);
    assertThat(entityMetadata, isSuccess());
    MetadataType generatedType = entityMetadata.get().getModel().getOutput().getType();
    assertThat(generatedType, is(instanceOf(ArrayType.class)));
    assertCircleType((ObjectType) ((ArrayType) generatedType).getType());
  }

  @Test
  public void getNativeQueryOutputMetadata() throws Exception {
    location = builder().globalName(NATIVE_QUERY_FLOW).addProcessorsPart().addIndexPart(0).build();
    MetadataKey nativeKey = newKey(NATIVE_QUERY).build();
    MetadataResult<ComponentMetadataDescriptor<OperationModel>> entityMetadata = getComponentDynamicMetadata(nativeKey);
    assertThat(entityMetadata, isSuccess());
    MetadataType generatedType = entityMetadata.get().getModel().getOutput().getType();
    assertCircleType((ObjectType) generatedType);
  }

  private void assertCircleType(ObjectType fields) {
    assertThat(fields.getFields(), hasSize(1));
    ObjectFieldType field = fields.getFields().iterator().next();
    assertThat(field.getKey().getName().getLocalPart(), is("id"));
    assertThat(field.getValue(), is(instanceOf(NumberType.class)));
  }
}
