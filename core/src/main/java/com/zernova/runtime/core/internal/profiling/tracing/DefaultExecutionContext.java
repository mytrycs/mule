/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling.tracing;

import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.profiling.tracing.ComponentMetadata;
import com.zernova.runtime.api.profiling.tracing.ExecutionContext;

import java.util.Optional;

/**
 * Immutable implementation of {@link ExecutionContext}
 */
public class DefaultExecutionContext implements ExecutionContext {

  private final ComponentMetadata componentMetadata;

  public DefaultExecutionContext(ComponentMetadata componentMetadata) {
    this.componentMetadata = componentMetadata;
  }

  @Override
  public Optional<ComponentMetadata> getCurrentComponentMetadata() {
    return ofNullable(componentMetadata);
  }
}
