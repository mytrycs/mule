/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.test.marvel.model.Missile;

@Alias("odd-missile")
public class OddMissileProvider extends MissileProvider implements Initialisable {

  private int count;

  @Override
  public void initialise() throws InitialisationException {
    count = -1;
  }

  @Override
  public Missile connect() throws ConnectionException {
    Missile missile = super.connect();
    missile.setArmed(++count % 2 == 0);

    return missile;
  }
}
