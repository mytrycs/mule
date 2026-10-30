/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.property;

import static java.util.Collections.unmodifiableList;

import com.zernova.runtime.api.meta.model.ModelProperty;

import java.util.List;

/**
 * {@link ModelProperty} to be used on Configurations or Connection Providers which indicates which parameters of these impact on
 * the metadata resolution.
 *
 * @since 1.2.0
 */
public class RequiredForMetadataModelProperty implements ModelProperty {

  public static final String NAME = "requiredForMetadata";

  private final List<String> requiredParameters;

  public RequiredForMetadataModelProperty(List<String> requiredParameters) {
    this.requiredParameters = requiredParameters;
  }

  /**
   * @return The list of required parameters for metadata resolution.
   */
  public List<String> getRequiredParameters() {
    return unmodifiableList(requiredParameters);
  }

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
    return true;
  }
}
