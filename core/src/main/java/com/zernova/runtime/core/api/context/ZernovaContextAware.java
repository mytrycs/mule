/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context;

import com.zernova.runtime.core.api.ZernovaContext;

/**
 * Objects who want to be aware of the ZernovaContext should implement this interface. Once the context has been initialised it will
 * be passed to all objects implementing this interface.
 *
 * @deprecated this interface is deprecated since {@link ZernovaContext} is deprecated. See {@link ZernovaContext} deprecation
 *             documentation for a replacement.
 */
@Deprecated
public interface ZernovaContextAware {

  /**
   * @param context the Mule node.
   */
  void setMuleContext(ZernovaContext context);
}
