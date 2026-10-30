/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.ObjectBuilder;

import org.slf4j.Logger;

public class RouteBuilderValueResolver extends ObjectBuilderValueResolver implements Lifecycle {

  private static final Logger LOGGER = getLogger(RouteBuilderValueResolver.class);

  private MessageProcessorChain nestedChain;

  public RouteBuilderValueResolver(ObjectBuilder builder, ZernovaContext muleContext, MessageProcessorChain chain) {
    super(builder, muleContext);
    this.nestedChain = chain;
  }

  @Override
  public void initialise() throws InitialisationException {
    super.initialise();
    if (nestedChain != null) {
      initialiseIfNeeded(nestedChain, muleContext);
    }
  }

  @Override
  public void dispose() {
    if (nestedChain != null) {
      disposeIfNeeded(nestedChain, LOGGER);
    }
  }

  @Override
  public void start() throws ZernovaException {
    if (nestedChain != null) {
      startIfNeeded(nestedChain);
    }
  }

  @Override
  public void stop() throws ZernovaException {
    if (nestedChain != null) {
      stopIfNeeded(nestedChain);
    }
  }
}
