/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metrics.impl.meter.builder;

import com.zernova.runtime.metrics.api.meter.builder.MeterBuilder;
import com.zernova.runtime.metrics.exporter.api.MeterExporter;
import com.zernova.runtime.metrics.impl.meter.repository.MeterRepository;

/**
 * A builder that has an internal repository for meters.
 */
public interface MeterBuilderWithRepository extends MeterBuilder {

  /**
   * @param meterRepository the meter repository to use.
   * @return the corresponding {@link MeterBuilder}
   */
  MeterBuilderWithRepository withMeterRepository(MeterRepository meterRepository);

  MeterBuilderWithRepository withMeterExporter(MeterExporter meterExporter);

}
