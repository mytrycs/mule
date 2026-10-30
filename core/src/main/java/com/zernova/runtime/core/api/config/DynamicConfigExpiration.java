/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.time.Time;
import com.zernova.runtime.api.time.TimeSupplier;
import com.zernova.runtime.core.internal.config.ImmutableDynamicConfigExpiration;
import com.zernova.runtime.core.internal.config.ImmutableExpirationPolicy;
import com.zernova.runtime.extension.api.runtime.ExpirationPolicy;

/**
 * Contains information about how much time should a dynamic config be idle before it can be considered elegible for expiration
 *
 * @since 4.0
 */
@NoImplement
public interface DynamicConfigExpiration {

  static DynamicConfigExpiration getDefault() {
    return new ImmutableDynamicConfigExpiration(new Time(5, MINUTES), ImmutableExpirationPolicy.getDefault());
  }

  static DynamicConfigExpiration getDefault(TimeSupplier timeSupplier) {
    return new ImmutableDynamicConfigExpiration(new Time(5, MINUTES), ImmutableExpirationPolicy.getDefault(timeSupplier));
  }

  /**
   * Returns the maximum amount of {@link Time} that a dynamic config instance can remain idle before it should be expired. This
   * does not mean that the instance will be expired exactly after that given amount of {@link Time}. The platform remains free to
   * perform the actual expiration at the frequency it sees fit
   *
   * @return a {@link Time}
   */
  Time getFrequency();

  /**
   * @return The {@link ExpirationPolicy} that will be applied
   */
  ExpirationPolicy getExpirationPolicy();
}
