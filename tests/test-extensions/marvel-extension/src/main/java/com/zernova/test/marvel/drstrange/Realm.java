/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.drstrange;

import static com.zernova.test.heisenberg.extension.HeisenbergExtension.HEISENBERG;

import static java.util.Objects.hash;

import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.reference.ConfigReference;

import java.util.Objects;

public class Realm {

  @ConfigReference(namespace = HEISENBERG, name = "config")
  @Parameter
  private String realmConfig;

  @Override
  public int hashCode() {
    return hash(realmConfig);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Realm other = (Realm) obj;
    return Objects.equals(realmConfig, other.realmConfig);
  }

}
