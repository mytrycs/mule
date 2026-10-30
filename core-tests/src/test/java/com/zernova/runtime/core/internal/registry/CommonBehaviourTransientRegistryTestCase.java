/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.registry;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.lifecycle.ZernovaLifecycleInterceptor;
import com.zernova.runtime.core.privileged.PrivilegedZernovaContext;
import com.zernova.tck.core.registry.AbstractRegistryTestCase;

import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommonBehaviourTransientRegistryTestCase extends AbstractRegistryTestCase {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS, extraInterfaces = {ZernovaContextWithRegistry.class, PrivilegedZernovaContext.class})
  private DefaultZernovaContext muleContext;

  @Override
  public Registry getRegistry() {
    return new SimpleRegistry(muleContext, new ZernovaLifecycleInterceptor());
  }
}
