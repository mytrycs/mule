/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context;

import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.config.api.LazyComponentInitializer;

/**
 * NoOp implementation for {@link LazyComponentInitializer}.
 */
public class NoOpLazyComponentInitializer implements LazyComponentInitializer {

  @Override
  public void initializeComponents(ComponentLocationFilter componentLocationFilter) {
    // Nothing to do...
  }

  @Override
  public void initializeComponent(Location location) {
    // Nothing to do...
  }
}
