/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.test.util;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.isReferableType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.impl.DefaultStringType;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;

import java.io.InputStream;
import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;

public class ExtensionMetadataTypeUtilsTestCase {

  private ClassTypeLoader typeLoader;

  @Before
  public void setUp() throws Exception {
    typeLoader = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();
  }

  @Test
  public void isReferableTypeForNonClassBaseTypes() {
    DefaultStringType type = BaseTypeBuilder.create(JAVA).stringType().build();
    assertThat(isReferableType(type), is(false));
  }

  @Test
  public void isReferableTypeInputStream() {
    assertThat(isReferableType(typeLoader.load(InputStream.class)), is(false));
  }

  @Test
  public void isReferableTypeObject() {
    assertThat(isReferableType(typeLoader.load(Object.class)), is(true));
  }

  @Test
  public void isReferableTypeSerializable() {
    assertThat(isReferableType(typeLoader.load(Serializable.class)), is(true));
  }
}
