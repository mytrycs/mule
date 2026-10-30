/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.streaming.object.factory;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.core.api.streaming.StreamingManager;

import java.io.Closeable;
import java.util.Iterator;

public class NullCursorIteratorProviderFactory extends AbstractCursorIteratorProviderFactory {

  public NullCursorIteratorProviderFactory(StreamingManager streamingManager) {
    super(streamingManager);
  }

  @Override
  protected Object resolve(Iterator iterator, EventContext eventContext, ComponentLocation originatingLocation) {
    if (iterator instanceof Closeable) {
      streamingManager.manage((Closeable) iterator, eventContext);
    }

    return iterator;
  }
}
