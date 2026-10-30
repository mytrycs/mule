/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.model.deprecated;

import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;

import java.util.Optional;

/**
 * Immutable implementation of {@link DeprecationModel}
 *
 * @since 1.2
 */
public class ImmutableDeprecationModel implements DeprecationModel {

  private final String message;
  private final String deprecatedSince;
  private final String toRemoveIn;

  public ImmutableDeprecationModel(String message, String deprecatedSince, String toRemoveIn) {
    this.message = message;
    this.deprecatedSince = deprecatedSince;
    this.toRemoveIn = toRemoveIn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMessage() {
    return message;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDeprecatedSince() {
    return deprecatedSince;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getToRemoveIn() {
    return ofNullable(toRemoveIn);
  }
}
