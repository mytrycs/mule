/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.processor;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.setMuleContextIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;

import java.util.List;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object that owns Mule objects and delegates startup/shutdown events to them.
 */
public abstract class AbstractZernovaObjectOwner<T> extends AbstractComponent
    implements Lifecycle, ZernovaContextAware {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  // TODO MULE-10332: Review ZernovaContextAware vs @Inject usage
  @Inject
  protected ZernovaContext muleContext;

  @Inject
  protected ConfigurationComponentLocator locator;

  @Override
  public void setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
    setMuleContextIfNeeded(getOwnedObjects(), muleContext);
  }

  public ZernovaContext getMuleContext() {
    return muleContext;
  }

  @Override
  public void initialise() throws InitialisationException {
    // TODO MULE-10764 This shouldn't happen here.
    setMuleContext(muleContext);
    initialiseIfNeeded(getOwnedObjects(), true, muleContext);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(getOwnedObjects(), logger);
  }


  @Override
  public void start() throws ZernovaException {
    startIfNeeded(getOwnedObjects());
  }


  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(getOwnedObjects());
  }

  protected abstract List<T> getOwnedObjects();

}
