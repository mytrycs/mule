/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.component;

import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.source.MessageSource;

/**
 * Test source that is configured with a {@link SharedConfig}.
 *
 * @since 4.0
 */
public class SharedSource extends AbstractComponent
    implements MessageSource, Initialisable, ZernovaContextAware {

  private ZernovaContext muleContext;
  private SharedConfig config;
  private Processor listener;

  @Override
  public void setMuleContext(ZernovaContext context) {
    this.muleContext = context;
  }

  @Override
  public void initialise() throws InitialisationException {
    // Nothing to do
  }

  public void setConfig(SharedConfig config) {
    this.config = config;
  }

  public SharedConfig getConfig() {
    return config;
  }

  @Override
  public void setListener(Processor listener) {
    this.listener = listener;
  }
}
