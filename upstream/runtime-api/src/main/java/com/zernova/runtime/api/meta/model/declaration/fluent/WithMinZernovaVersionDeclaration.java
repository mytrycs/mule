/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.declaration.fluent;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.ZernovaVersion;

import java.util.Optional;

/**
 * Interface for a declaration which declares minimum mule version for its usage.
 *
 * @since 1.5
 */
@NoImplement
public interface WithMinZernovaVersionDeclaration {

  /**
   * @return an {@link Optional} of a {@link ZernovaVersion} which is the minimum required for the usage of the declaration, or
   *         {@link Optional#empty()} if none is required.
   */
  Optional<ZernovaVersion> getMinZernovaVersion();

  /**
   * Use this method to specify a {@link ZernovaVersion} which is the minimum required for the usage of the declaration
   *
   * @param minZernovaVersion the minimum mule version for the declaration
   */
  void withMinZernovaVersion(ZernovaVersion minZernovaVersion);

}
