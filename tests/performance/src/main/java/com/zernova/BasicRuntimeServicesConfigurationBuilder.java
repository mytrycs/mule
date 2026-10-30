/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.privileged.registry.LegacyRegistryUtils.registerObject;
import static com.zernova.tck.config.WeaveExpressionLanguageFactoryServiceProvider.provideDefaultExpressionLanguageFactoryService;

import com.zernova.runtime.api.el.DefaultExpressionLanguageFactoryService;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.tck.SimpleUnitTestSupportSchedulerService;

/**
 * Provides the basic {@link Service}s infrastructure required by the Mule runtime to start in embedded mode.
 *
 * @since 4.0
 */
public class BasicRuntimeServicesConfigurationBuilder extends AbstractConfigurationBuilder {

  @Override
  protected void doConfigure(ZernovaContext muleContext) throws Exception {
    final SimpleUnitTestSupportSchedulerService simpleUnitTestSupportSchedulerService =
        new SimpleUnitTestSupportSchedulerService();

    try {
      startIfNeeded(simpleUnitTestSupportSchedulerService);
      registerObject(muleContext, simpleUnitTestSupportSchedulerService.getName(), simpleUnitTestSupportSchedulerService);
    } catch (ZernovaException e) {
      throw new ZernovaRuntimeException(e);
    }

    DefaultExpressionLanguageFactoryService weaveExpressionExecutor = provideDefaultExpressionLanguageFactoryService();
    registerObject(muleContext, weaveExpressionExecutor.getName(), weaveExpressionExecutor);
  }
}
