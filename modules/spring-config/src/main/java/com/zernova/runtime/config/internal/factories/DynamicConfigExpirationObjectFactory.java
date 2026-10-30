/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import com.zernova.runtime.api.time.Time;
import com.zernova.runtime.api.time.TimeSupplier;
import com.zernova.runtime.core.api.config.DynamicConfigExpiration;
import com.zernova.runtime.core.internal.config.ImmutableDynamicConfigExpiration;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;
import com.zernova.runtime.dsl.api.component.ObjectFactory;
import com.zernova.runtime.extension.api.runtime.ExpirationPolicy;
import com.zernova.runtime.core.internal.config.ImmutableExpirationPolicy;

import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

/**
 * An {@link ObjectFactory} which produces instances of {@link DynamicConfigExpiration}
 */
public class DynamicConfigExpirationObjectFactory extends AbstractComponentFactory<DynamicConfigExpiration> {

  @Inject
  private TimeSupplier timeSupplier;

  private final long frequency;
  private final TimeUnit timeUnit;
  private ExpirationPolicy expirationPolicy;

  public DynamicConfigExpirationObjectFactory(long frequency, TimeUnit timeUnit) {
    this.frequency = frequency;
    this.timeUnit = timeUnit;
  }

  @Override
  public DynamicConfigExpiration doGetObject() throws Exception {
    return new ImmutableDynamicConfigExpiration(new Time(frequency, timeUnit), getExpirationPolicy());
  }

  public void setExpirationPolicy(ExpirationPolicy expirationPolicy) {
    this.expirationPolicy = expirationPolicy;
  }

  private ExpirationPolicy getExpirationPolicy() {
    if (expirationPolicy != null) {
      return expirationPolicy;
    }

    return ImmutableExpirationPolicy.getDefault(timeSupplier);
  }
}
