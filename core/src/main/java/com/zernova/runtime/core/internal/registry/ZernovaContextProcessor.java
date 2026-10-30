/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.registry;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.privileged.registry.InjectProcessor;

/**
 * Injects the ZernovaContext object for objects stored in the {@link Registry} where the object registered implements
 * {@link com.zernova.runtime.core.api.context.ZernovaContextAware}.
 *
 * @deprecated as of 3.7.0 since these are only used by {@link Registry} which is also deprecated. Use post processors for
 *             currently supported registries instead
 */
@Deprecated
// TODO W-10781591 Remove this
public class ZernovaContextProcessor implements InjectProcessor {

  private final ZernovaContext context;

  public ZernovaContextProcessor(ZernovaContext context) {
    this.context = context;
  }

  @Override
  public Object process(Object object) {
    if (object instanceof ZernovaContextAware) {
      ((ZernovaContextAware) object).setMuleContext(context);
    }
    return object;
  }
}
