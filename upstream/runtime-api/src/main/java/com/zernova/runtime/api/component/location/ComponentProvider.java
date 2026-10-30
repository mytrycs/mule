/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.component.location;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.component.Component;

/**
 * Interface for all objects that may have a relationship to an element in the configuration.
 * <p>
 * Common use case of this interface is to be implemented by exceptions that are related to failures in the creation/execution of
 * the component.
 *
 * @since 1.0
 */
@NoImplement
public interface ComponentProvider {

  /**
   * @return the element in the configuration.
   */
  Component getComponent();

}
