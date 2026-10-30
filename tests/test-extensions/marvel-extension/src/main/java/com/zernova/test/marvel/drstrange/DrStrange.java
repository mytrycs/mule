/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.drstrange;

import static com.zernova.test.marvel.MarvelExtension.MARVEL_EXTENSION;
import static com.zernova.test.marvel.drstrange.DrStrange.CONFIG_NAME;

import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.reference.ConfigReference;
import com.zernova.runtime.extension.api.annotation.param.reference.ObjectStoreReference;
import com.zernova.sdk.api.annotation.connectivity.ConnectionProviders;
import com.zernova.test.marvel.ironman.IronMan;

import jakarta.inject.Inject;

/**
 * Default extension to test cursor streams and lists. Given Dr. Strange's ability to use the eye of Agamotto to reverse and
 * advance time, he's uniquely positioned to manipulate such streams
 */
@Configuration(name = CONFIG_NAME)
@Operations(DrStrangeOperations.class)
@Sources(DrStrangeBytesSource.class)
@ConnectionProviders({MysticConnectionProvider.class, PoolingMysticConnectionProvider.class})
public class DrStrange implements Initialisable {

  public static final String CONFIG_NAME = "dr-strange";

  @Inject
  private ConfigurationComponentLocator locator;

  @ConfigReference(namespace = MARVEL_EXTENSION, name = IronMan.CONFIG_NAME)
  @Parameter
  @Optional
  private String ironManConfig;

  @Parameter
  @Optional
  @ObjectStoreReference
  private String spellStore;

  @Override
  public void initialise() throws InitialisationException {
    if (ironManConfig != null) {
      locator.find(Location.builder().globalName(ironManConfig).build())
          .orElseThrow(() -> new IllegalArgumentException("No bean with name " + ironManConfig));
    }
  }

}
