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
 * The type of beginning actions that sources can take regarding transactions.
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
public enum SourceTransactionalAction {

  /**
   * Will ensure that a new transaction is created for each invocation
   * <p>
   * J2EE RequiresNew
   */
  ALWAYS_BEGIN,

  /**
   * Whether there is a transaction available or not, ignore it
   * <p>
   * J2EE: NotSupported
   */
  NONE
}
