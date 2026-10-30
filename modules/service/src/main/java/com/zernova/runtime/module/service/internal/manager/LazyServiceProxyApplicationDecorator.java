/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.internal.manager;

import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.module.service.api.discoverer.ServiceAssembly;

/**
 * A subclass of {@link LazyServiceProxy} ment to be used in the context of a deployed application.
 *
 * This subclass uses an alternate {@link MethodInvoker} while also ignores all lifecycle invocations.
 *
 * @since 4.2
 */
public class LazyServiceProxyApplicationDecorator extends LazyServiceProxy {


  public LazyServiceProxyApplicationDecorator(ServiceAssembly assembly,
                                              DefaultServiceRegistry serviceRegistry,
                                              LazyValue<Service> service,
                                              MethodInvoker methodInvoker) {
    super(assembly, serviceRegistry, service);
    setMethodInvoker(methodInvoker);
  }

  @Override
  protected Object handleStart() {
    return null;
  }

  @Override
  protected Object handleStop() {
    return null;
  }
}
