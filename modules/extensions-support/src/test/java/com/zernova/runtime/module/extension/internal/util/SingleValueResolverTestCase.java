/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getField;

import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.test.heisenberg.extension.model.PersonalInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class SingleValueResolverTestCase extends AbstractZernovaTestCase {

  private static final String NAME = "name";

  @Mock
  private ParameterModel parameterModel;

  @Mock
  private ResolverSetResult result;


  private ValueSetter valueSetter;

  @Before
  public void before() {
    when(result.get(NAME)).thenReturn(NAME);
    valueSetter = new SingleValueSetter(NAME, getField(PersonalInfo.class, "name", new ReflectionCache()).get());
  }

  @Test
  public void set() throws Exception {
    PersonalInfo info = new PersonalInfo();
    valueSetter.set(info, result);
    assertThat(info.getName(), is(NAME));
  }
}
