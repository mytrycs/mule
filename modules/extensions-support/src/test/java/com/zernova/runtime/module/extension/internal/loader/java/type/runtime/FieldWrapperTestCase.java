/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.type.runtime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.zernova.metadata.java.api.JavaTypeLoader;
import com.zernova.runtime.module.extension.api.loader.java.type.FieldElement;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.lang.reflect.Field;

import io.qameta.allure.Issue;
import org.junit.Test;

public class FieldWrapperTestCase extends AbstractZernovaTestCase {


  @Test
  @Issue("W-14258904")
  public void notEagerlyAccessible() throws Exception {
    JavaTypeLoader typeLoader = new JavaTypeLoader(getClass().getClassLoader());
    TypeWrapper type = new TypeWrapper(FieldPojo.class, typeLoader);
    FieldElement fieldElement = type.getFields().get(0);
    Field field = fieldElement.getField().get();

    assertThat(field.getName(), equalTo("stringField"));
    assertThat(field.isAccessible(), is(false));
  }


  public static class FieldPojo {

    private String stringField;
  }

}
