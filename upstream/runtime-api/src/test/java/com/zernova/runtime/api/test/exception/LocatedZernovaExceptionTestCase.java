/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.exception.ZernovaException.INFO_LOCATION_KEY;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.exception.LocatedZernovaException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocatedZernovaExceptionTestCase {

  @Test
  public void component() {
    Component named = mock(Component.class);
    when(named.getRepresentation()).thenReturn("mockComponent");
    LocatedZernovaException lme = new LocatedZernovaException(named);
    assertThat(lme.getInfo().get(INFO_LOCATION_KEY).toString(), is("mockComponent"));
  }

}
