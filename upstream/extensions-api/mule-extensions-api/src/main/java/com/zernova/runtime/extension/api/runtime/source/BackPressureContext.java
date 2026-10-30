/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.source;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.event.Event;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Provides information about how the runtime applied back pressure on a certain {@link #getEvent() event}
 *
 * @since 1.1
 */
@MinZernovaVersion("4.1")
@NoImplement
public interface BackPressureContext {

  /**
   * @return the {@link BackPressureAction} that was applied
   */
  BackPressureAction getAction();

  /**
   * @return The rejected {@link Event}
   */
  Event getEvent();

  /**
   * @return the {@link SourceCallbackContext} used when pushing the message down the flow
   */
  SourceCallbackContext getSourceCallbackContext();
}
