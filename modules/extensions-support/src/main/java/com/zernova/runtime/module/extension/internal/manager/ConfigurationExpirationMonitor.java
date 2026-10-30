/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.manager;

/**
 * Monitors configuration instances looking for those which should be expired.
 * <p/>
 * It only exposes {@link #beginMonitoring()} and {@link #stopMonitoring()} method to signal when monitoring should start/stop.
 *
 * @since 4.0
 */
public interface ConfigurationExpirationMonitor {

  /**
   * Begins monitoring and disposal of expired configuration instances
   */
  void beginMonitoring();

  /**
   * Stops monitoring and disposal of expired configuration instances
   */
  void stopMonitoring();
}
