/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import com.zernova.runtime.api.time.TimeSupplier;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;
import com.zernova.runtime.dsl.api.component.ObjectFactory;
import com.zernova.runtime.extension.api.runtime.ExpirationPolicy;
import com.zernova.runtime.core.internal.config.ImmutableExpirationPolicy;

import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

/**
 * An {@link ObjectFactory} which produces instances of {@link ExpirationPolicy}
 *
 * @since 4.0
 */
public class ExpirationPolicyObjectFactory extends AbstractComponentFactory<ExpirationPolicy> {

  @Inject
  private TimeSupplier timeSupplier;

  private Long maxIdleTime = null;
  private TimeUnit timeUnit = null;

  public void setMaxIdleTime(Long maxIdleTime) {
    this.maxIdleTime = maxIdleTime;
  }

  public void setTimeUnit(TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
  }

  @Override
  public ExpirationPolicy doGetObject() throws Exception {
    if (maxIdleTime != null && timeUnit != null) {
      return new ImmutableExpirationPolicy(maxIdleTime, timeUnit, timeSupplier);
    }
    return ImmutableExpirationPolicy.getDefault(timeSupplier);
  }
}
