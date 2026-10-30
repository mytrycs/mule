/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.ironman;

import static com.zernova.test.marvel.ironman.IronMan.CONFIG_NAME;
import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.display.ClassValue;
import com.zernova.runtime.extension.api.annotation.param.display.Path;
import com.zernova.test.marvel.MissileProvider;
import com.zernova.test.marvel.OddMissileProvider;
import com.zernova.test.marvel.model.Missile;

@Configuration(name = CONFIG_NAME)
@Operations({IronManOperations.class, JarvisOperations.class})
@ConnectionProviders({MissileProvider.class, OddMissileProvider.class})
public class IronMan {

  public static final String CONFIG_NAME = "ironMan";

  @Path(acceptsUrls = true)
  @Parameter
  @Optional
  private String ironManConfigFile;

  @ClassValue(extendsOrImplements = "com.starkindustries.AIEngine")
  @Parameter
  @Optional(defaultValue = "com.starkindustries.Jarvis")
  private String aiType;

  private String flightPlan = null;
  private int missilesFired = 0;

  public void track(Missile missile) {
    missilesFired++;
  }

  public int getMissilesFired() {
    return missilesFired;
  }

  public String getFlightPlan() {
    return flightPlan;
  }

  public void setFlightPlan(String flightPlan) {
    this.flightPlan = flightPlan;
  }
}
