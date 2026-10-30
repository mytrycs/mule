/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api;

import com.zernova.runtime.api.exception.ZernovaException;

/**
 * Marking interface to identity resources that need to be closed in order to release resources.
 */
@Deprecated
public interface Closeable {

  /**
   * Closes the resource. Calling this method is mandatory for any component using this instance once it finishes using it. This
   * method should not throw exception is invoked on an instance that has already been closed
   *
   * @throws ZernovaException if an exception occurs closing the resource
   */
  void close() throws ZernovaException;

}
