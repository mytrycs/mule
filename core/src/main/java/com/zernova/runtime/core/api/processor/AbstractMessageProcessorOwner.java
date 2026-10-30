/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.processor;

import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.core.api.context.ZernovaContextAware;

import java.util.List;

/**
 * An object that owns message processors and delegates startup/shutdown events to them.
 */
public abstract class AbstractMessageProcessorOwner extends AbstractZernovaObjectOwner<Processor>
    implements Lifecycle, ZernovaContextAware, Component {

  @Override
  protected List<Processor> getOwnedObjects() {
    return getOwnedMessageProcessors();
  }

  protected abstract List<Processor> getOwnedMessageProcessors();

}

