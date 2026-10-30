/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.registry;

import static java.lang.Thread.currentThread;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.core.internal.registry.TestDiscoverableObject;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.Collection;

import org.junit.Test;

@SmallTest
public class SpiServiceRegistryTestCase extends AbstractZernovaTestCase {

  private ServiceRegistry serviceRegistry = new SpiServiceRegistry();

  @Test
  public void lookupProviders() throws Exception {
    Collection<TestDiscoverableObject> discoveredObjects =
        serviceRegistry.lookupProviders(TestDiscoverableObject.class, currentThread().getContextClassLoader());
    assertProvidersFound(discoveredObjects);
  }

  @Test
  public void lookupProvidersWithCustomClassLoader() throws Exception {
    ClassLoader parent = mock(ClassLoader.class);
    ClassLoader classLoader = mock(ClassLoader.class, RETURNS_DEEP_STUBS);
    when(classLoader.getParent()).thenReturn(parent);
    serviceRegistry.lookupProviders(TestDiscoverableObject.class, classLoader);

    verify(classLoader).getResources("META-INF/services/" + TestDiscoverableObject.class.getName());
  }

  private void assertProvidersFound(Collection<TestDiscoverableObject> discoveredObjects) {
    assertThat(discoveredObjects, hasSize(1));
    assertThat(discoveredObjects.iterator().next(), instanceOf(TestDiscoverableObject.class));
  }

}
