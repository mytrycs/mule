/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.troubleshooting.api;

import com.zernova.api.annotation.Experimental;

/**
 * Interface that has the {@link TroubleshootingOperationDefinition} and the {@link TroubleshootingOperationCallback} for a given
 * operation.
 *
 * @since 4.5
 */
@Experimental
public interface TroubleshootingOperation {

  /**
   * Returns the {@link TroubleshootingOperationDefinition} for this operation.
   *
   * @return the {@link TroubleshootingOperationDefinition} for this operation.
   */
  TroubleshootingOperationDefinition getDefinition();

  /**
   * Returns the {@link TroubleshootingOperationCallback} for this operation.
   *
   * @return the {@link TroubleshootingOperationCallback} for this operation.
   */

  TroubleshootingOperationCallback getCallback();
}
