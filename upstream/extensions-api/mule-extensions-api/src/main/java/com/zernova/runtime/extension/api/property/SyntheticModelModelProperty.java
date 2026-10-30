/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.property;

import com.zernova.runtime.api.meta.model.ModelProperty;

/**
 * {@link ModelProperty} to be used on any {@link com.zernova.runtime.api.meta.model.EnrichableModel} that it's added to to a
 * {@link com.zernova.runtime.api.meta.model.ExtensionModel} by the Mule's Sdk.
 *
 * @since 1.2.0
 */
public class SyntheticModelModelProperty implements ModelProperty {

  public static final String NAME = "syntheticModel";

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isPublic() {
    return false;
  }
}
