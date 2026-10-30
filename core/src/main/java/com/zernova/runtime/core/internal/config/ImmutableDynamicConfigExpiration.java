/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.time.Time;
import com.zernova.runtime.core.api.config.DynamicConfigExpiration;
import com.zernova.runtime.extension.api.runtime.ExpirationPolicy;

/**
 * Immutable implementation of {@link DynamicConfigExpiration}
 *
 * @since 1.0
 */
public class ImmutableDynamicConfigExpiration extends AbstractComponent implements DynamicConfigExpiration {

  private final Time frequency;
  private final ExpirationPolicy expirationPolicy;

  public ImmutableDynamicConfigExpiration(Time frequency, ExpirationPolicy expirationPolicy) {
    this.frequency = frequency;
    this.expirationPolicy = expirationPolicy;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Time getFrequency() {
    return frequency;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExpirationPolicy getExpirationPolicy() {
    return expirationPolicy;
  }
}
