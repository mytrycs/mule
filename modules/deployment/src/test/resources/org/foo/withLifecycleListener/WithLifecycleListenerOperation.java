/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.withLifecycleListener;

import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;

import com.zernova.runtime.core.api.ZernovaContext;

import jakarta.inject.Inject;

public class WithLifecycleListenerOperation {

    @Inject
    private ZernovaContext muleContext;

    public void leak() {
        // Leaks a Thread that will have the Execution ClassLoader as its TCCL.
        new LeakedThread().start();
    }
}
