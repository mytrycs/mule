/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.config.profiling;

/**
 * The status for a {@link com.zernova.runtime.api.profiling.ProfilingDataProducer}.
 */
public interface ProfilingDataProducerStatus {

  /**
   * @return whether it is enabled or disabled.
   */
  boolean isEnabled();

  /**
   * resets the status. Useful for propagating changes if needed.
   */
  void reset();
}
