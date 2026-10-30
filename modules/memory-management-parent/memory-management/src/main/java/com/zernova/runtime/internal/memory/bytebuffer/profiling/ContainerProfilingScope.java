/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.internal.memory.bytebuffer.profiling;

import static com.zernova.runtime.api.profiling.ProfilingProducerScopeType.CONTAINER_SCOPE_TYPE;

import com.zernova.runtime.api.profiling.ProfilingProducerScope;
import com.zernova.runtime.api.profiling.ProfilingProducerScopeType;

/**
 * A {@link ProfilingProducerScope} tht represents the whole container
 */
public class ContainerProfilingScope implements ProfilingProducerScope {

  @Override
  public String getProducerScopeIdentifier() {
    return "";
  }

  @Override
  public ProfilingProducerScopeType getProducerScopeTypeIdentifier() {
    return CONTAINER_SCOPE_TYPE;
  }
}
