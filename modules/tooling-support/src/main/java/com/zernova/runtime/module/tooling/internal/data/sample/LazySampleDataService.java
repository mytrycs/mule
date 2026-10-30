/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.data.sample;

import static com.zernova.runtime.api.exception.ExceptionHelper.getRootException;
import static com.zernova.runtime.extension.api.values.ValueResolvingException.INVALID_LOCATION;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.config.internal.context.lazy.LazyComponentInitializerAdapter;
import com.zernova.runtime.config.internal.context.lazy.NoSuchComponentModelException;
import com.zernova.runtime.core.api.data.sample.SampleDataService;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.sdk.api.data.sample.SampleDataException;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * {@link SampleDataService} implementation that initialises just the required components before executing the resolving logic.
 * <p>
 * This guarantees that if the application has been created lazily, the requested components exists before the execution of the
 * actual {@link SampleDataService}.
 *
 * @since 4.4.0
 * @see SampleDataService
 */
public class LazySampleDataService implements SampleDataService, Initialisable {

  public static final String NON_LAZY_SAMPLE_DATA_SERVICE = "_muleNonLazySampleDataService";
  private final Function<Registry, SampleDataService> sampleDataServiceSupplier;

  @Inject
  private Registry registry;

  @Inject
  private LazyComponentInitializerAdapter lazyComponentInitializer;

  @Inject
  @Named(NON_LAZY_SAMPLE_DATA_SERVICE)
  private SampleDataService sampleDataService;

  public LazySampleDataService(Function<Registry, SampleDataService> sampleDataServiceSupplier) {
    this.sampleDataServiceSupplier = sampleDataServiceSupplier;
  }

  @Override
  public void initialise() throws InitialisationException {
    this.sampleDataService = sampleDataServiceSupplier.apply(registry);
  }

  @Override
  public Message getSampleData(Location location) throws SampleDataException {
    try {
      lazyComponentInitializer.initializeComponent(location, false);
    } catch (Exception e) {
      Throwable rootException = getRootException(e);
      if (rootException instanceof NoSuchComponentModelException) {
        throw new SampleDataException(e.getMessage(), INVALID_LOCATION, e);
      }
      throw e;
    }
    return sampleDataService.getSampleData(location);
  }

  @Override
  public Message getSampleData(String extensionName, String componentName, Map<String, Object> parameters,
                               Supplier<Optional<ConfigurationInstance>> configurationInstanceSupplier)
      throws SampleDataException {
    return sampleDataService.getSampleData(extensionName, componentName, parameters, configurationInstanceSupplier);
  }
}
