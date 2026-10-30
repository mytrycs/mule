/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.event;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.core.api.event.CoreEvent;

/**
 * Provides capability to only accept handling certain MuleEvents.
 */
@NoImplement
public interface Acceptor {

  /**
   * @param event {@link CoreEvent} to route through exception handler
   * @return true if this {@link com.zernova.runtime.core.api.exception.FlowExceptionHandler} should handler exception false
   *         otherwise
   */
  boolean accept(CoreEvent event);

  /**
   * @return true if accepts any message, false otherwise.
   */
  boolean acceptsAll();
}
