/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.zernova.runtime.api.util.MultiMap;

import org.junit.Test;

public class CustomOAuthParametersTestCase {

  @Test
  public void testGetQueryParams() {
    CustomOAuthParameters parameters = new CustomOAuthParameters();
    MultiMap<String, String> result = parameters.getQueryParams();

    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(0));
  }

  @Test
  public void testGetHeaders() {
    CustomOAuthParameters parameters = new CustomOAuthParameters();
    MultiMap<String, String> result = parameters.getHeaders();

    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(0));
  }

  @Test
  public void testGetBodyParams() {
    CustomOAuthParameters parameters = new CustomOAuthParameters();
    MultiMap<String, String> result = parameters.getBodyParams();

    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(0));
  }
}
