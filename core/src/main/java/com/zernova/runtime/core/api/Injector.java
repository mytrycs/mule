/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.exception.ZernovaException;

/**
 * Component capable of injecting dependencies into a given object
 *
 * @since 3.7.0
 */
@NoImplement
public interface Injector {

  /**
   * Injects dependencies into the given object
   *
   * @param object the object on which dependencies are to be injected on
   * @return the injected object or a proxy to it
   * @throws ZernovaException
   */
  <T> T inject(T object) throws ZernovaException;
}
