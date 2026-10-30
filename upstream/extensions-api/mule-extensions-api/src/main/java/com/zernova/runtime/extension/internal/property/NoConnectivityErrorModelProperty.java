/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.property;

import com.zernova.runtime.api.meta.model.ModelProperty;

/**
 * Marker {@link ModelProperty} for modules to avoid propagating connectivity errors from operations.
 *
 * @since 1.8
 */
public class NoConnectivityErrorModelProperty implements ModelProperty {

  private static final long serialVersionUID = 1L;

  @Override
  public String getName() {
    return "noConnectivityError";
  }

  @Override
  public boolean isPublic() {
    return false;
  }
}
