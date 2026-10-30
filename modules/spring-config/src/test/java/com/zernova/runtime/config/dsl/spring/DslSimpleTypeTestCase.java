/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.dsl.spring;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static com.zernova.runtime.dsl.api.component.DslSimpleType.isSimpleType;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

@SmallTest
public class DslSimpleTypeTestCase extends AbstractZernovaTestCase {

  private enum TestEnum {
    TEST
  }

  @Test
  public void simpleTypes() {
    assertThat(isSimpleType(byte.class), is(true));
    assertThat(isSimpleType(Byte.class), is(true));
    assertThat(isSimpleType(short.class), is(true));
    assertThat(isSimpleType(Short.class), is(true));
    assertThat(isSimpleType(int.class), is(true));
    assertThat(isSimpleType(Integer.class), is(true));
    assertThat(isSimpleType(long.class), is(true));
    assertThat(isSimpleType(Long.class), is(true));
    assertThat(isSimpleType(float.class), is(true));
    assertThat(isSimpleType(Float.class), is(true));
    assertThat(isSimpleType(double.class), is(true));
    assertThat(isSimpleType(Double.class), is(true));
    assertThat(isSimpleType(char.class), is(true));
    assertThat(isSimpleType(Character.class), is(true));
    assertThat(isSimpleType(String.class), is(true));
    assertThat(isSimpleType(Object.class), is(false));
    assertThat(isSimpleType(TestEnum.class), is(true));
    assertThat(isSimpleType(AtomicInteger.class), is(false));
    assertThat(isSimpleType(Date.class), is(true));
    assertThat(isSimpleType(LocalDate.class), is(true));
    assertThat(isSimpleType(Calendar.class), is(true));
    assertThat(isSimpleType(LocalDateTime.class), is(true));
    assertThat(isSimpleType(BigDecimal.class), is(true));
    assertThat(isSimpleType(BigInteger.class), is(true));
  }

}
