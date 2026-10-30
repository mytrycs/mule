/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.parameter;

import com.zernova.api.annotation.NoImplement;

import java.util.Set;

/**
 * Indicates that some of the optional {@link ParameterModel parameters} in a {@link ParameterGroupModel} are mutually exclusive
 * and cannot be defined at the same time.
 *
 * @since 1.0
 */
@NoImplement
public interface ExclusiveParametersModel {

  /**
   * @return The names of the mutually exclusive parameters
   */
  Set<String> getExclusiveParameterNames();

  /**
   * Indicates if besides of being exclusive, the runtime should enforce that on of the parameters MUST be provided
   */
  boolean isOneRequired();
}
