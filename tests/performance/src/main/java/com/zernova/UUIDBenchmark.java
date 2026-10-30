/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova;

import static com.zernova.runtime.core.api.util.UUID.getClusterUUID;
import static com.zernova.runtime.core.api.util.UUID.getUUID;

import com.zernova.runtime.api.exception.ZernovaException;

import org.openjdk.jmh.annotations.Benchmark;

public class UUIDBenchmark extends AbstractBenchmark {

  @Benchmark
  public String UUID() throws ZernovaException {
    return getUUID();
  }

  @Benchmark
  public String clusterUUID() throws ZernovaException {
    return getClusterUUID(getClusterId());
  }

  @Benchmark
  public String clusterUUIDPrefix() throws ZernovaException {
    return getClusterUUID(getClusterIdPrefix());
  }

  private int getClusterId() {
    return 1;
  }

  private String getClusterIdPrefix() {
    return "1-";
  }

}
