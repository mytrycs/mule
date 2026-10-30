/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataFormat;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.NumberType;
import com.zernova.metadata.api.model.ObjectFieldType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.extension.api.dsql.DsqlParser;
import com.zernova.runtime.extension.api.dsql.DsqlQuery;
import com.zernova.runtime.extension.api.metadata.NullMetadataResolver;
import com.zernova.tck.size.SmallTest;
import com.zernova.test.metadata.extension.query.MetadataExtensionEntityResolver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class DsqlQueryMetadataResolverTestCase {

  private static final DsqlParser dsqlParser = DsqlParser.getInstance();

  @Mock
  private MetadataContext context;

  @Before
  public void setUp() {
    when(context.getTypeBuilder()).thenReturn(new BaseTypeBuilder(MetadataFormat.JAVA));
  }

  @Test
  public void getTrimmedOutputMetadata() throws MetadataResolvingException, ConnectionException {
    DsqlQuery dsqlQuery = dsqlParser.parse("dsql:SELECT id FROM Circle WHERE (diameter < 18)");
    MetadataType outputMetadata = getQueryMetadataResolver().getOutputType(context, dsqlQuery);

    ObjectType type = getAndAssertTypeOf(outputMetadata);
    assertThat(type.getFields(), hasSize(1));
    ObjectFieldType onlyField = type.getFields().iterator().next();
    assertThat(onlyField.getValue(), is(instanceOf(NumberType.class)));
    assertThat(onlyField.getKey().getName().getLocalPart(), is("id"));
  }

  @Test
  public void getFullOutputMetadata() throws MetadataResolvingException, ConnectionException {
    DsqlQuery dsqlQuery = dsqlParser.parse("dsql:SELECT * FROM Circle WHERE (diameter < 18)");
    MetadataType outputMetadata = getQueryMetadataResolver().getOutputType(context, dsqlQuery);

    ObjectType type = getAndAssertTypeOf(outputMetadata);
    assertThat(type.getFields(), hasSize(3));

    type.getFields().forEach(f -> {
      String name = f.getKey().getName().getLocalPart();
      assertThat(name, isIn(asList("color", "id", "diameter")));
    });
  }

  private ObjectType getAndAssertTypeOf(MetadataType outputMetadata) {
    assertThat(outputMetadata, is(instanceOf(ObjectType.class)));
    return (ObjectType) outputMetadata;
  }

  private DsqlQueryMetadataResolver getQueryMetadataResolver() {
    NullMetadataResolver outputResolver = new NullMetadataResolver();
    MetadataExtensionEntityResolver entityResolver = new MetadataExtensionEntityResolver();
    return new DsqlQueryMetadataResolver(entityResolver, outputResolver);
  }
}
