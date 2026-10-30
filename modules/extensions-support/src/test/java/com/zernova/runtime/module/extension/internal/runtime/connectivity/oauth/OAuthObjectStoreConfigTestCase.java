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

import org.junit.Test;

public class OAuthObjectStoreConfigTestCase {

  @Test
  public void testObjectStoreNameInitialization() {
    String expectedName = "test-store";
    OAuthObjectStoreConfig config = new OAuthObjectStoreConfig(expectedName);

    assertThat(config.getObjectStoreName(), is(expectedName));
  }
}
