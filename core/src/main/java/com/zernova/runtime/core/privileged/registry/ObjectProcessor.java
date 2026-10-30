/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.registry;

import com.zernova.runtime.core.internal.registry.Registry;

/**
 * The parent interface for all object processors. Object processors can be registered in the Mule registry and fired at the
 * correct time.
 * <p/>
 * Developers must not implement this interface directly. Instead use either
 * {@link com.zernova.runtime.core.privileged.registry.InjectProcessor} or
 * {@link com.zernova.runtime.core.privileged.registry.PreInitProcessor}.
 *
 * @deprecated as of 3.7.0 since these are only used by {@link Registry} which is also deprecated. Use post processors for
 *             currently supported registries instead
 */
@Deprecated
// TODO W-10781591 Remove this
public interface ObjectProcessor {

  Object process(Object object);
}
