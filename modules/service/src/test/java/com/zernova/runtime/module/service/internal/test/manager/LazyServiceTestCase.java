/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.internal.test.manager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.api.service.ServiceDefinition;
import com.zernova.runtime.api.service.ServiceProvider;
import com.zernova.runtime.module.service.api.discoverer.ServiceAssembly;
import com.zernova.runtime.module.service.internal.manager.DefaultServiceRegistry;
import com.zernova.runtime.module.service.internal.manager.LazyServiceProxy;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.verification.VerificationMode;

public class LazyServiceTestCase extends AbstractZernovaTestCase {

  private static final String TEST_VALUE = "dubby dubby do";
  private static final String SERVICE_NAME = "test service";

  @Rule
  public MockitoRule mockitorule = MockitoJUnit.rule();

  @Mock
  private ServiceAssembly assembly;

  @Mock
  private ServiceProvider serviceProvider;

  @Mock
  private DefaultServiceRegistry serviceRegistry;

  private TestService lazyService;

  private TestService actualService;

  @Before
  public void before() {
    when(assembly.getName()).thenReturn(SERVICE_NAME);
    when(assembly.getServiceContract()).thenReturn((Class) TestService.class);
    when(assembly.getClassLoader()).thenReturn(TestService.class.getClassLoader());
    when(assembly.getServiceProvider()).thenReturn(serviceProvider);

    actualService = spy(new TestServiceImpl());
    when(serviceProvider.getServiceDefinition()).thenReturn(new ServiceDefinition(TestService.class, actualService));

    lazyService = (TestService) LazyServiceProxy.from(assembly, serviceRegistry, null);
  }

  @Test
  public void invokeRealMethod() throws Exception {
    assertThat(lazyService.doSomething(), equalTo(TEST_VALUE));
    assertInitialised();
    verify(actualService).doSomething();
  }

  @Test
  public void assertLifecycleNotPropagatedIfNotInitialised() throws Exception {
    lazyService.start();
    assertNotInitialised();
    verify(actualService, never()).start();
    lazyService.stop();
    assertNotInitialised();
    verify(actualService, never()).stop();
  }

  @Test
  public void assertLifecyclePropagatedWhenInitialised() throws Exception {
    lazyService.start();
    assertNotInitialised();
    invokeRealMethod();
    verify(actualService).start();

    lazyService.stop();
    verify(actualService).stop();
  }

  @Test
  public void assertLifecyclePropagatedIfInitialised() throws Exception {
    invokeRealMethod();
    lazyService.start();
    verify(actualService).start();

    lazyService.stop();
    verify(actualService).stop();
  }

  @Test
  public void getNameDoesNotInitialise() throws Exception {
    assertThat(lazyService.getName(), equalTo(SERVICE_NAME));
    assertNotInitialised();
  }

  @Test
  public void equalsDoesNotInitialise() throws Exception {
    assertThat(lazyService.equals(mock(Service.class)), is(false));
    assertThat(lazyService.equals(lazyService), is(true));

    assertNotInitialised();
  }

  @Test
  public void hashCodeDoesNotInitialise() throws Exception {
    assertThat(lazyService.equals(mock(Service.class)), is(false));
    assertThat(lazyService.equals(lazyService), is(true));

    assertNotInitialised();
  }

  private void assertInitialised() throws Exception {
    assertInitialisation(atLeastOnce());
    verify(serviceRegistry).inject(serviceProvider);
  }

  private void assertNotInitialised() throws Exception {
    assertInitialisation(never());
    verify(serviceRegistry, never()).inject(serviceProvider);
  }

  private void assertInitialisation(VerificationMode mode) throws Exception {
    verify(assembly, mode).getClassLoader();
    verify(assembly, mode).getServiceProvider();
  }

  public interface TestService extends Service, Startable, Stoppable {

    String doSomething();
  }

  public class TestServiceImpl implements TestService {

    @Override
    public String doSomething() {
      return TEST_VALUE;
    }

    @Override
    public String getName() {
      return SERVICE_NAME;
    }

    @Override
    public void start() throws ZernovaException {

    }

    @Override
    public void stop() throws ZernovaException {

    }
  }
}
