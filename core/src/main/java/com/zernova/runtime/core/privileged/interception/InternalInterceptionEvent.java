/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.interception;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.interception.InterceptionEvent;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.event.PrivilegedEvent;

/**
 * Adds support for compatibility attribues of the {@link CoreEvent} to {@link InterceptionEvent}.
 *
 * @since 4.0
 */
@NoImplement
public interface InternalInterceptionEvent extends InterceptionEvent {

  /**
   * Updates the state of this object if needed.
   *
   * @return {@link PrivilegedEvent} with the result.
   */
  PrivilegedEvent resolve();

}
