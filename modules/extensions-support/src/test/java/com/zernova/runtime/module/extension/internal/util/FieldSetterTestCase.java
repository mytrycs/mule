/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.tck.testmodels.fruit.Apple;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.reflections.ReflectionUtils;

@SmallTest
public class FieldSetterTestCase extends AbstractZernovaTestCase {

  private FieldSetter<Apple, Boolean> setter;
  private Apple apple = new Apple(false);
  private Field field;

  @Before
  public void before() throws Exception {
    field = ReflectionUtils.getAllFields(Apple.class, ReflectionUtils.withName("bitten")).iterator().next();
    setter = new FieldSetter<>(field);
  }

  @Test
  public void set() throws Exception {
    setter.set(apple, true);
    assertThat(apple.isBitten(), is(true));
  }

  @Test
  public void getField() {
    assertThat(setter.getField(), is(sameInstance(field)));
  }
}
