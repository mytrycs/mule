/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.Parameters;
import static com.zernova.metadata.java.api.JavaTypeLoader.JAVA;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.visitor.BasicTypeMetadataVisitor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SmallTest
@RunWith(Parameterized.class)
public class BasicTypeMetadataVisitorTestCase extends AbstractZernovaTestCase {

  private static final BaseTypeBuilder BUILDER = BaseTypeBuilder.create(JAVA);

  @Parameters(name = "isSimpleType({0})")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {{BUILDER.stringType().build(), true}, {BUILDER.numberType().build(), true},
        {BUILDER.booleanType().build(), true}, {BUILDER.objectType().build(), false},
        {BUILDER.arrayType().of(BUILDER.stringType()).build(), false}, {BUILDER.dateTimeType().build(), false}});
  }

  @Parameterized.Parameter(0)
  public MetadataType metadataType;

  @Parameterized.Parameter(1)
  public boolean expectedSimple;

  private BasicTypeMetadataVisitor visitor = new BasicTypeMetadataVisitor() {

    @Override
    protected void visitBasicType(MetadataType metadataType) {
      simpleType = true;
    }

    @Override
    protected void defaultVisit(MetadataType metadataType) {
      complexType = true;
    }
  };

  private boolean simpleType;
  private boolean complexType;

  @Before
  public void before() {
    simpleType = false;
    complexType = false;
  }

  @Test
  public void assertSimpleOrNot() {
    metadataType.accept(visitor);
    assertThat(simpleType, is(expectedSimple));
    assertThat(complexType, is(!expectedSimple));
  }
}
