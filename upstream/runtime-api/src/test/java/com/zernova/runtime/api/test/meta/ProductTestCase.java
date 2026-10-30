/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test.meta;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static com.zernova.runtime.api.deployment.meta.Product.ZERNOVA;
import static com.zernova.runtime.api.deployment.meta.Product.ZERNOVA_EE;
import static com.zernova.runtime.api.deployment.meta.Product.getProductByName;

import org.junit.Test;

public class ProductTestCase {

  @Test
  public void productSupport() {
    assertThat(ZERNOVA.supports(ZERNOVA), is(true));
    assertThat(ZERNOVA.supports(ZERNOVA_EE), is(false));
    assertThat(ZERNOVA_EE.supports(ZERNOVA), is(true));
    assertThat(ZERNOVA_EE.supports(ZERNOVA_EE), is(true));
  }

  @Test
  public void productByProductName() {
    assertThat(getProductByName(null), is(ZERNOVA_EE));
    assertThat(getProductByName("Mule Core"), is(ZERNOVA));
    assertThat(getProductByName("Mule EE Core"), is(ZERNOVA_EE));
  }

}
