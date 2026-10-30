/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.version;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.OutputModel;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;

import java.util.Optional;

/**
 * Interface for a model which declares minimum mule version for its usage.
 *
 * @since 1.5
 */
@NoImplement
public interface HasMinZernovaVersion {

  /**
   * @return an {@link Optional} {@link ZernovaVersion} which is the minimum required for the usage of the model, or
   *         {@link Optional#empty()} if none is required.
   */
  Optional<ZernovaVersion> getMinZernovaVersion();

}
