/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.extension.api.runtime.source.BackPressureAction;
import com.zernova.runtime.extension.api.runtime.source.BackPressureContext;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;

/**
 * Immutable implementation of {@link BackPressureContext}
 *
 * @since 1.1
 */
public final class ImmutableBackPressureContext implements BackPressureContext {

  private final Event event;
  private final BackPressureAction action;
  private final SourceCallbackContext sourceCallbackContext;

  public ImmutableBackPressureContext(Event event,
                                      BackPressureAction action,
                                      SourceCallbackContext sourceCallbackContext) {
    this.event = event;
    this.action = action;
    this.sourceCallbackContext = sourceCallbackContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Event getEvent() {
    return event;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public BackPressureAction getAction() {
    return action;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SourceCallbackContext getSourceCallbackContext() {
    return sourceCallbackContext;
  }
}
