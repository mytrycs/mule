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
import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.api.streaming.object.CursorIteratorProvider;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.streaming.object.InMemoryCursorIteratorConfig;
import com.zernova.runtime.core.internal.streaming.object.InMemoryCursorIteratorProvider;

import java.util.Iterator;

public class InMemoryCursorIteratorProviderFactory extends AbstractCursorIteratorProviderFactory {

  private final InMemoryCursorIteratorConfig config;

  /**
   * Creates a new instance
   *
   * @param config the config for the generated providers
   */
  public InMemoryCursorIteratorProviderFactory(InMemoryCursorIteratorConfig config, StreamingManager streamingManager) {
    super(streamingManager);
    this.config = config;
  }

  /**
   * {@inheritDoc}
   *
   * @return a new {@link CursorIteratorProvider} wrapped in an {@link Either}
   */
  @Override
  protected Object resolve(Iterator iterator, EventContext eventContext, ComponentLocation originatingLocation) {
    return new InMemoryCursorIteratorProvider(iterator, config, originatingLocation, trackCursorProviderClose);
  }
}
