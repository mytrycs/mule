/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metrics.impl;

import static com.zernova.runtime.metrics.impl.meter.DefaultMeter.builder;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.metrics.api.MeterProvider;
import com.zernova.runtime.metrics.api.meter.builder.MeterBuilder;
import com.zernova.runtime.metrics.exporter.api.MeterExporter;
import com.zernova.runtime.metrics.exporter.api.MeterExporterFactory;
import com.zernova.runtime.metrics.exporter.config.api.MeterExporterConfiguration;
import com.zernova.runtime.metrics.impl.meter.repository.MeterRepository;

import jakarta.inject.Inject;

/**
 * A default implementation of the {@link MeterProvider}
 */
public class DefaultMeterProvider implements MeterProvider, Disposable {

  private final MeterExporterFactory meterExporterFactory;
  private final MeterExporterConfiguration meterExporterConfiguration;
  private final MeterRepository meterRepository = new MeterRepository();
  private final LazyValue<MeterExporter> meterExporter = new LazyValue<>(this::resolveMeterExporter);

  @Inject
  public DefaultMeterProvider(MeterExporterFactory meterExporterFactory, MeterExporterConfiguration meterExporterConfiguration) {
    this.meterExporterFactory = meterExporterFactory;
    this.meterExporterConfiguration = meterExporterConfiguration;
  }

  private MeterExporter resolveMeterExporter() {
    return meterExporterFactory.getMeterExporter(meterExporterConfiguration);
  }

  @Override
  public MeterBuilder getMeterBuilder(String meterName) {
    return builder(meterName)
        .withMeterExporter(meterExporter.get())
        .withMeterRepository(meterRepository);
  }

  public MeterRepository getMeterRepository() {
    return meterRepository;
  }

  @Override
  public void dispose() {
    meterExporter.get().dispose();
  }
}
