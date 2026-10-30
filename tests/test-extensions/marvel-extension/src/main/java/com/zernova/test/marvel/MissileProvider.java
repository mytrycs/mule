/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;
import static com.zernova.runtime.api.meta.model.display.PathModel.Type.DIRECTORY;
import static com.zernova.test.marvel.MissileProvider.NAME;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.semantics.file.FilePath;
import com.zernova.test.marvel.model.Missile;

@Alias(NAME)
public class MissileProvider implements ConnectionProvider<Missile> {

  public static final String NAME = "missile";

  @Parameter
  @Optional
  @FilePath(type = DIRECTORY)
  private String missileConfigurationDirectory;

  @Override
  public Missile connect() throws ConnectionException {
    return new Missile();
  }

  @Override
  public void disconnect(Missile missile) {
    missile.setArmed(false);
  }

  @Override
  public ConnectionValidationResult validate(Missile connection) {
    return success();
  }
}
