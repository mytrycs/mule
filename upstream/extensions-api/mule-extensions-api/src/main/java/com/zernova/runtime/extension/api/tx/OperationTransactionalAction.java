/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.tx;

import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * The type of joining actions that operations can take regarding transactions.
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
public enum OperationTransactionalAction {
  /**
   * There must always be a transaction present for the invocation
   * <p>
   * J2EE: Mandatory
   */
  ALWAYS_JOIN,

  /**
   * If there is a transaction available, then use it, otherwise continue processing
   * <p>
   * J2EE: Supports
   */
  JOIN_IF_POSSIBLE,


  /**
   * Executes outside any existent transaction
   */
  NOT_SUPPORTED
}
