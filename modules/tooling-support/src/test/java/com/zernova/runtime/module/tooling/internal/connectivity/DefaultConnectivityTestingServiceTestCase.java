/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.connectivity;

import static com.zernova.runtime.api.component.location.Location.builder;
import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;
import static com.zernova.tck.junit4.matcher.connection.ConnectionValidationResultFailureMatcher.isFailure;
import static com.zernova.tck.junit4.matcher.connection.ConnectionValidationResultSuccessMatcher.isSuccess;
import static com.zernova.tck.util.ZernovaContextUtils.registerIntoMockContext;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.connectivity.ConnectivityTestingStrategy;
import com.zernova.runtime.api.connectivity.UnsupportedConnectivityTestingObjectException;
import com.zernova.runtime.api.exception.ObjectNotFoundException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.module.tooling.internal.connectivity.DefaultConnectivityTestingService;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DefaultConnectivityTestingServiceTestCase extends AbstractZernovaTestCase {

  private static final String TEST_IDENTIFIER = "testIdentifier";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private final ZernovaContextWithRegistry mockMuleContext = mock(ZernovaContextWithRegistry.class, RETURNS_DEEP_STUBS);
  private final ConnectivityTestingStrategy mockConnectivityTestingStrategy =
      mock(ConnectivityTestingStrategy.class, RETURNS_DEEP_STUBS);
  private DefaultConnectivityTestingService connectivityTestingService;
  private final Component fakeConnectivityTestingObject = mock(Component.class);

  @Before
  public void createConnectivityService() throws InitialisationException {
    connectivityTestingService = new DefaultConnectivityTestingService();
    connectivityTestingService.setServiceRegistry(() -> asList(mockConnectivityTestingStrategy).stream());
    connectivityTestingService.setMuleContext(mockMuleContext);
    connectivityTestingService.setLocator(mockMuleContext.getConfigurationComponentLocator());

    when(mockMuleContext.getConfigurationComponentLocator().find(any(Location.class)))
        .thenReturn(of(fakeConnectivityTestingObject));
    when(mockConnectivityTestingStrategy.accepts(fakeConnectivityTestingObject)).thenReturn(true);
    registerIntoMockContext(mockMuleContext, TEST_IDENTIFIER, fakeConnectivityTestingObject);
    connectivityTestingService.initialise();
  }

  @Test
  public void testConnectionThrowsException() throws Exception {
    final RuntimeException exception = new RuntimeException();
    when(mockConnectivityTestingStrategy.testConnectivity(fakeConnectivityTestingObject)).thenThrow(exception);
    final ConnectionValidationResult validationResult =
        connectivityTestingService.testConnection(builder().globalName(TEST_IDENTIFIER).build());

    assertThat(validationResult, isFailure());
    assertThat(validationResult.getException(), is(exception));
  }

  @Test
  public void testConnection() {
    when(mockConnectivityTestingStrategy.testConnectivity(fakeConnectivityTestingObject)).thenReturn(success());
    final ConnectionValidationResult validationResult =
        connectivityTestingService.testConnection(builder().globalName(TEST_IDENTIFIER).build());
    assertThat(validationResult, isSuccess());
  }

  @Test
  public void testObjectNotSupported() {
    reset(mockConnectivityTestingStrategy);
    when(mockConnectivityTestingStrategy.accepts(fakeConnectivityTestingObject)).thenReturn(false);
    expectedException.expect(UnsupportedConnectivityTestingObjectException.class);
    connectivityTestingService.testConnection(builder().globalName(TEST_IDENTIFIER).build());
  }

  @Test
  public void nonExistentConnectivityTestingObject() {
    reset(mockMuleContext);
    when(mockMuleContext.getConfigurationComponentLocator().find(any(Location.class))).thenReturn(empty());

    connectivityTestingService.setLocator(mockMuleContext.getConfigurationComponentLocator());
    expectedException.expect(ObjectNotFoundException.class);

    connectivityTestingService.testConnection(builder().globalName(TEST_IDENTIFIER).build());
  }

}
