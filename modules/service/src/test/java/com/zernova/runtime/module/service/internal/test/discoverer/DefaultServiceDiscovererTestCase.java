/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.internal.test.discoverer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.module.service.api.discoverer.ServiceAssembly;
import com.zernova.runtime.module.service.api.discoverer.ServiceProviderDiscoverer;
import com.zernova.runtime.module.service.api.discoverer.ServiceResolutionError;
import com.zernova.runtime.module.service.internal.discoverer.DefaultServiceDiscoverer;
import com.zernova.runtime.module.service.internal.discoverer.ServiceResolver;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class DefaultServiceDiscovererTestCase extends AbstractZernovaTestCase {

  private final ServiceResolver serviceResolver = mock(ServiceResolver.class);
  private final ServiceProviderDiscoverer serviceProviderDiscoverer = mock(ServiceProviderDiscoverer.class);
  private final DefaultServiceDiscoverer serviceDiscoverer =
      new DefaultServiceDiscoverer(serviceProviderDiscoverer, serviceResolver);

  @Test
  public void discoversServices() throws Exception {
    final List<ServiceAssembly> serviceProviders = new ArrayList<>();
    when(serviceProviderDiscoverer.discover()).thenReturn(serviceProviders);

    final List<Service> expectedServices = new ArrayList<>();
    when(serviceResolver.resolveServices(serviceProviders)).thenReturn(expectedServices);

    final List<Service> services = serviceDiscoverer.discoverServices();

    assertThat(services, is(expectedServices));
  }

  @Test(expected = ServiceResolutionError.class)
  public void propagatesDiscoverErrors() throws Exception {
    when(serviceProviderDiscoverer.discover()).thenThrow(new ServiceResolutionError("Error"));

    serviceDiscoverer.discoverServices();
  }

  @Test(expected = ServiceResolutionError.class)
  public void propagatesServiceResolutionErrors() throws Exception {
    final List<ServiceAssembly> serviceProviders = new ArrayList<>();
    when(serviceProviderDiscoverer.discover()).thenReturn(serviceProviders);
    when(serviceResolver.resolveServices(serviceProviders)).thenThrow(new RuntimeException(new ServiceResolutionError("Error")));

    serviceDiscoverer.discoverServices();
  }
}
