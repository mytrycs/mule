/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.internal.memory.management;

import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.profiling.ProfilingService;

/**
 * A {@link MemoryManagementService} that can profile the usage of memory resources using the {@link ProfilingService} set.
 */
public interface ProfiledMemoryManagementService extends MemoryManagementService {

  /**
   * The {@link ProfilingService} used by the implementation for profiling the memory resources usage.
   *
   * @param profilingService the {@link ProfilingService} used by the implementation to track/profile resources.
   */
  void setProfilingService(ProfilingService profilingService);
}
