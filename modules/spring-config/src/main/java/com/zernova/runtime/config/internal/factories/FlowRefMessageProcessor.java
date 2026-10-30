/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import static com.zernova.runtime.core.privileged.processor.MessageProcessors.processToApply;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.processor.AnnotatedProcessor;

/**
 * Defines a common base class for flow-ref processors.
 *
 * @Since 4.3.0
 */
public abstract class FlowRefMessageProcessor extends AbstractComponent
    implements AnnotatedProcessor, Startable, Stoppable, Disposable {

  private final FlowRefFactoryBean owner;

  protected FlowRefMessageProcessor(FlowRefFactoryBean owner) {
    this.owner = owner;
  }

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    return processToApply(event, this);
  }

  @Override
  public void start() throws ZernovaException {
    this.doStart();
  }

  @Override
  public ComponentLocation getLocation() {
    return owner.getLocation();
  }

  public abstract void doStart() throws ZernovaException;

  public FlowRefFactoryBean getOwner() {
    return owner;
  }
}
