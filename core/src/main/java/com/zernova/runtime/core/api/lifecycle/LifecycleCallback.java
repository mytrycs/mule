/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.lifecycle;

import com.zernova.runtime.api.exception.ZernovaException;

/**
 * This callback is used to execute lifecycle behaviour for an object being managed by a {@link LifecycleManager} The callback is
 * used so that transitions can be managed consistently outside of an object
 *
 * @since 3.0
 */
public interface LifecycleCallback<O> {

  void onTransition(String phaseName, O object) throws ZernovaException;
}
