/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metadata.api.locator;

import com.zernova.runtime.api.component.location.Location;

import java.util.Optional;

@FunctionalInterface
public interface ComponentLocator<C> {

  /**
   * @param location the location of a Component
   * @return the Component present at the given {@link Location}, or {@link Optional#empty()} if none was found.
   */
  Optional<C> get(Location location);

}
