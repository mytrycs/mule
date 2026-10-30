/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config.builders;

import static com.zernova.test.allure.AllureConstants.RegistryFeature.REGISTRY;
import static com.zernova.test.allure.AllureConstants.RegistryFeature.ObjectRegistrationStory.OBJECT_REGISTRATION;

import static java.util.Collections.singletonList;
import static java.util.Optional.of;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.config.custom.CustomizationService.ServiceInterceptor;
import com.zernova.runtime.core.api.Injector;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.config.bootstrap.BootstrapServiceDiscoverer;
import com.zernova.runtime.core.api.config.bootstrap.PropertiesBootstrapService;
import com.zernova.runtime.core.internal.config.CustomService;
import com.zernova.runtime.core.internal.config.InternalCustomizationService;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.lifecycle.DefaultLifecycleManager;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@SmallTest
@Feature(REGISTRY)
@Story(OBJECT_REGISTRATION)
public class MinimalConfigurationBuilderTestCase extends AbstractZernovaTestCase {

  private static final String SERVICE_ID = "serviceId";

  private ZernovaContextWithRegistry muleContext;
  private ZernovaRegistry registry;
  private BootstrapServiceDiscoverer bootstrapServiceDiscoverer;
  private InternalCustomizationService customizationService;

  @Before
  public void setUp() {
    registry = mock(ZernovaRegistry.class);
    bootstrapServiceDiscoverer = mock(BootstrapServiceDiscoverer.class);
    customizationService = mock(InternalCustomizationService.class);

    muleContext = mock(ZernovaContextWithRegistry.class);
    when(muleContext.getRegistry()).thenReturn(registry);
    when(muleContext.getRegistryBootstrapServiceDiscoverer()).thenReturn(bootstrapServiceDiscoverer);
    when(muleContext.getCustomizationService()).thenReturn(customizationService);
    when(muleContext.getExecutionClassLoader()).thenReturn(this.getClass().getClassLoader());

    when(muleContext.getConfiguration()).thenReturn(mock(ZernovaConfiguration.class));
    when(muleContext.getInjector()).thenReturn(mock(Injector.class));
    when(muleContext.getLifecycleManager()).thenReturn(new DefaultLifecycleManager<>("muleContext", muleContext));
  }

  @Test
  @Issue("W-17146121")
  public void override() throws Exception {
    final Properties properties = new Properties();
    final Object overridenService = new ServiceObjectB();

    properties.put(SERVICE_ID, ServiceObjectA.class.getName());
    when(bootstrapServiceDiscoverer.discover())
        .thenReturn(singletonList(new PropertiesBootstrapService(this.getClass().getClassLoader(),
                                                                 properties)));
    when(customizationService.getOverriddenService(SERVICE_ID))
        .thenReturn(of(new CustomService<>(SERVICE_ID, si -> si.overrideServiceImpl(overridenService), false)));

    final MinimalConfigurationBuilder minimalConfigurationBuilder = new MinimalConfigurationBuilder();

    minimalConfigurationBuilder.doConfigure(muleContext);

    verify(registry, never()).registerObject(eq(SERVICE_ID), any(ServiceObjectA.class));
    verify(registry).registerObject(eq(SERVICE_ID), same(overridenService));
  }

  @Test
  @Issue("W-17146121")
  public void remove() throws Exception {
    final Properties properties = new Properties();

    properties.put(SERVICE_ID, ServiceObjectA.class.getName());
    when(bootstrapServiceDiscoverer.discover())
        .thenReturn(singletonList(new PropertiesBootstrapService(this.getClass().getClassLoader(),
                                                                 properties)));
    when(customizationService.getOverriddenService(SERVICE_ID))
        .thenReturn(of(new CustomService<>(SERVICE_ID, ServiceInterceptor::remove, false)));

    final MinimalConfigurationBuilder minimalConfigurationBuilder = new MinimalConfigurationBuilder();

    minimalConfigurationBuilder.doConfigure(muleContext);

    verify(registry, never()).registerObject(eq(SERVICE_ID), any(ServiceObjectA.class));
  }

  public static class ServiceObjectA {

  }

  public static class ServiceObjectB {

  }
}
