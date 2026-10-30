/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.withLifecycleListener;

import com.zernova.sdk.api.annotation.Extension;
import com.zernova.sdk.api.annotation.OnArtifactLifecycle;
import com.zernova.sdk.api.annotation.Operations;

/**
 * Extension with LifecycleListener, for testing purposes
 */
@Extension(name = "WithLifecycleListener")
@OnArtifactLifecycle(LifecycleListener.class)
@Operations({WithLifecycleListenerOperation.class})
public class WithLifecycleListenerExtension {
}
