/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.api.factories.streaming;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;

import jakarta.inject.Inject;

@NoExtend
public abstract class AbstractCursorProviderObjectFactory<T> extends AbstractComponentFactory<T> {

  @Inject
  protected StreamingManager streamingManager;

  @Inject
  protected FeatureFlaggingService ffService;

  @Override
  public T getObject() throws Exception {
    initialiseIfNeeded(streamingManager);
    return doGetObject();
  }

}
