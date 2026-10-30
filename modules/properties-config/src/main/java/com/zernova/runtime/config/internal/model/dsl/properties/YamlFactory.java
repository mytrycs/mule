/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.model.dsl.properties;

import org.vibur.objectpool.PoolObjectFactory;
import org.yaml.snakeyaml.Yaml;

public class YamlFactory implements PoolObjectFactory<Yaml> {

  @Override
  public Yaml create() {
    return new Yaml();
  }

  @Override
  public boolean readyToTake(Yaml obj) {
    return true;
  }

  @Override
  public boolean readyToRestore(Yaml obj) {
    return true;
  }

  @Override
  public void destroy(Yaml obj) {
    // Nothing to do
  }

}
