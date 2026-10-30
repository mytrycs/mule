/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor.simple;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;

/**
 * Base class for {@link Processor}s that change the event or message. Implementations will return a new instance of the event
 * with the updated data, since the {@link CoreEvent} and {@link Message} objects are immutable.
 *
 * @since 4.0
 */
public abstract class SimpleMessageProcessor extends AbstractComponent
    implements Processor, ZernovaContextAware, Initialisable {

  protected ZernovaContext muleContext;

  @Override
  public void setMuleContext(ZernovaContext context) {
    this.muleContext = context;
  }

}
