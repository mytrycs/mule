/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.component;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.source.MessageSource;

/**
 * Test source that provides access to the {@link Processor} set by the owner {@link com.zernova.runtime.core.api.construct.Flow}.
 *
 * @since 4.0
 */
public class SkeletonSource extends AbstractComponent implements MessageSource, Startable {

  private Processor listener;
  private volatile boolean started = false;

  @Override
  public void setListener(Processor listener) {
    this.listener = listener;
  }

  public Processor getListener() {
    return listener;
  }

  @Override
  public synchronized void start() throws ZernovaException {
    started = true;
  }

  public synchronized boolean isStarted() {
    return started;
  }
}
