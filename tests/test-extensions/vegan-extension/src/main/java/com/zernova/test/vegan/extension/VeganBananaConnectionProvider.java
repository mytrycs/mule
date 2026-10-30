/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.ConfigOverride;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.tck.testmodels.fruit.Banana;

@Alias("BananaPlantain")
public class VeganBananaConnectionProvider implements ConnectionProvider<Banana> {

  @Parameter
  @Optional(defaultValue = "Ecuador")
  String originCountry;

  @Override
  public Banana connect() throws ConnectionException {
    Banana banana = new Banana();
    banana.setOrigin(originCountry);
    return banana;
  }

  @Override
  public void disconnect(Banana banana) {

  }

  @Override
  public ConnectionValidationResult validate(Banana banana) {
    return ConnectionValidationResult.success();
  }
}
