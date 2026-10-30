/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;
import com.zernova.runtime.core.privileged.registry.RegistrationException;

import org.junit.Test;

public class LazyLookupTestCase {

  @Test
  public void testSuccessfulLookup() throws RegistrationException {
    Class<String> type = String.class;
    ZernovaContext muleContext = mock(ZernovaContextWithRegistry.class);
    ZernovaRegistry registry = mock(ZernovaRegistry.class);
    when(((ZernovaContextWithRegistry) muleContext).getRegistry()).thenReturn(registry);
    when(registry.lookupObject(type)).thenReturn("TestValue");

    LazyLookup<String> lazyLookup = new LazyLookup<>(type, muleContext);
    assertThat(lazyLookup.get(), is("TestValue"));
  }

  @Test(expected = ZernovaRuntimeException.class)
  public void testLookupThrowsException() {
    Class<String> type = String.class;
    ZernovaContext muleContext = mock(ZernovaContextWithRegistry.class);
    when(((ZernovaContextWithRegistry) muleContext).getRegistry()).thenThrow(new RuntimeException("Registry failure"));

    LazyLookup<String> lazyLookup = new LazyLookup<>(type, muleContext);
    lazyLookup.get();
  }
}
