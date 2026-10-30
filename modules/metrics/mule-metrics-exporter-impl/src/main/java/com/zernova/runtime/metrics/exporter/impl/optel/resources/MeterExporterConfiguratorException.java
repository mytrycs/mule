/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metrics.exporter.impl.optel.resources;

/**
 * An Exception raised during configuration.
 *
 * @since 4.5.0
 */
public class MeterExporterConfiguratorException extends RuntimeException {

  public MeterExporterConfiguratorException(Exception e) {
    super(e);
  }

  public MeterExporterConfiguratorException(String s) {
    super(s);
  }
}
