/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.lifecycle;

/**
 * {@code DefaultLifecyclePhase} adds lifecycle methods {@code start}, {@code stop} and {@code dispose}.
 *
 * @since 1.0
 */
public interface Lifecycle extends Initialisable, Startable, Stoppable, Disposable {
  // empty
}
