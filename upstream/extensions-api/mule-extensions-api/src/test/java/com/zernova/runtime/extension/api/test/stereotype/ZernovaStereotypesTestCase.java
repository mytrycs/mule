/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.test.stereotype;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.OBJECT_STORE;

import com.zernova.runtime.extension.api.stereotype.ObjectStoreStereotype;

import org.junit.Test;

public class ZernovaStereotypesTestCase {

  @Test
  public void objectStore() {
    ObjectStoreStereotype objectStoreStereotype = new ObjectStoreStereotype();
    assertThat(OBJECT_STORE.getNamespace(), equalTo(objectStoreStereotype.getNamespace()));
    assertThat(OBJECT_STORE.getType(), equalTo(objectStoreStereotype.getName()));
  }

}
