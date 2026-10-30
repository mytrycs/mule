/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.drstrange;

import static com.zernova.sdk.api.connectivity.ConnectionValidationResult.success;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.HEISENBERG;
import static com.zernova.test.marvel.MarvelExtension.MARVEL_EXTENSION;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.reference.ConfigReference;
import com.zernova.sdk.api.connectivity.ConnectionProvider;
import com.zernova.sdk.api.connectivity.ConnectionValidationResult;
import com.zernova.test.marvel.ironman.IronMan;

@Alias("mystic")
public class MysticConnectionProvider implements ConnectionProvider<MysticConnection> {

  @ConfigReference(namespace = HEISENBERG, name = "config")
  @ConfigReference(namespace = MARVEL_EXTENSION, name = IronMan.CONFIG_NAME)
  @Parameter
  @Optional
  private String multipleConfig;

  @Parameter
  @Optional
  private Realm targetRealm;

  @Override
  public MysticConnection connect() throws ConnectionException {
    return new MysticConnection();
  }

  @Override
  public void disconnect(MysticConnection connection) {
    connection.close();
  }

  @Override
  public ConnectionValidationResult validate(MysticConnection connection) {
    return success();
  }
}
