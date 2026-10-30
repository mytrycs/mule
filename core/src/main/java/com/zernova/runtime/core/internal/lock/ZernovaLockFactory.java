/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.lock;

import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_LOCK_PROVIDER;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lock.LockFactory;
import com.zernova.runtime.api.lock.LockProvider;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;

import java.util.concurrent.locks.Lock;

import jakarta.inject.Inject;
import jakarta.inject.Named;

public class ZernovaLockFactory implements LockFactory, Initialisable, Disposable {

  private LockGroup lockGroup;
  private LockProvider lockProvider;

  @Inject
  private SchedulerService schedulerService;

  // TODO (W-11958289): This ZernovaContext is injected just to obtain the ZernovaConfiguration, so the ZernovaConfiguration
  // should be injected instead. However, when injecting that class here it makes the test
  // LazyInitConfigurationComponentLocatorTestCase fail because the ZernovaLockFactory is instantiated twice.
  @Inject
  private ZernovaContext muleContext;

  @Override
  public synchronized Lock createLock(String lockId) {
    return new LockAdapter(lockId, lockGroup, schedulerService);
  }

  @Override
  public void dispose() {
    if (lockGroup != null) {
      lockGroup.dispose();
    }
  }

  @Override
  public void initialise() throws InitialisationException {
    lockGroup = createLockGroup();
  }

  @Inject
  @Named(OBJECT_LOCK_PROVIDER)
  public void setLockProvider(LockProvider lockProvider) {
    this.lockProvider = lockProvider;
  }

  private LockGroup createLockGroup() {
    // This class is created programmatically, and in such case the mule context isn't injected.
    if (muleContext == null) {
      return new InstanceLockGroup(lockProvider);
    }

    ZernovaConfiguration muleConfiguration = muleContext.getConfiguration();
    if (muleConfiguration == null) {
      return new InstanceLockGroup(lockProvider);
    }

    return new InstanceLockGroup(lockProvider, muleConfiguration.getShutdownTimeout());
  }
}
