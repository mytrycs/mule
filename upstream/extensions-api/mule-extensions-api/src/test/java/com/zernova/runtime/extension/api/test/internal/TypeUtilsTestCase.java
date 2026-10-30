/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.test.internal;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.declaration.type.TypeUtils;

import java.lang.reflect.Field;
import java.util.Collection;

import org.junit.Test;

public class TypeUtilsTestCase {

  @Test
  public void getParameterFields() {
    Collection<Field> fields = TypeUtils.getParameterFields(TestObject.class);
    assertThat(fields, hasSize(3));

    assertThat(fields.stream().map(Field::getName).collect(toList()),
               containsInAnyOrder("name", "description", "group"));
  }


  public static class TestObject {

    @Parameter
    private String name;

    @Parameter
    private String description;

    @ParameterGroup(name = "group")
    private TestParameterGroup group;
  }


  public static class TestParameterGroup {

    @Parameter
    private String groupParameter;
  }
}
