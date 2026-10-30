/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.lifecycle;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.lifecycle.LifecycleCallback;

/**
 * A lifecycle callback that does nothing. Can be used to transition a
 * {@link com.zernova.runtime.core.api.lifecycle.LifecycleManager} to the next phase without executing logic.
 *
 * USers should never use this object themselves, it provides an internal Mule function.
 *
 * @since 3.0
 */
public class EmptyLifecycleCallback<O> implements LifecycleCallback<O> {

  public void onTransition(String phaseName, O object) throws ZernovaException {
    // do nothing
  }
}
