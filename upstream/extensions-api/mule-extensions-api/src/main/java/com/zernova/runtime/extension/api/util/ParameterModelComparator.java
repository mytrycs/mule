/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.util;

import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.isInfrastructure;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;

import java.util.Comparator;

/**
 * A {@link Comparator} for sorting objects of type {@link ParameterModel}
 */
public class ParameterModelComparator implements Comparator<ParameterModel> {

  private final boolean infrastructureFirst;

  /**
   * Creates a new instance
   *
   * @param infrastructureFirst whether infrastructure parameters should be at the beginning or the end
   */
  public ParameterModelComparator(boolean infrastructureFirst) {
    this.infrastructureFirst = infrastructureFirst;
  }

  @Override
  public int compare(ParameterModel left, ParameterModel right) {
    boolean isLeftInfrastructure = isInfrastructure(left);
    boolean isRightInfrastructure = isInfrastructure(right);

    if (!isLeftInfrastructure && !isRightInfrastructure) {
      return 0;
    }

    if (isLeftInfrastructure) {
      return infrastructureFirst ? -1 : 1;
    }

    if (isRightInfrastructure) {
      return infrastructureFirst ? 1 : -1;
    }

    return left.getName().compareTo(right.getName());
  }
}
