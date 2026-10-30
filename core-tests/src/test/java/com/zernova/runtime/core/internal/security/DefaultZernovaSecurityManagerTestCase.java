/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.security;

import static java.util.Optional.ofNullable;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.security.Authentication;
import com.zernova.runtime.api.security.SecurityException;
import com.zernova.runtime.api.security.SecurityProviderNotFoundException;
import com.zernova.runtime.api.security.UnauthorisedException;
import com.zernova.runtime.core.api.security.SecurityManager;
import com.zernova.runtime.core.api.security.SecurityProvider;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


@SmallTest
public class DefaultZernovaSecurityManagerTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Mock
  private SecurityProvider provider;

  @Mock
  private Authentication authentication;

  @Rule
  public ExpectedException expected = none();

  private SecurityManager manager;

  @Before
  public void setUp() {
    when(provider.getName()).thenReturn("provider");
    when(provider.supports(any())).thenReturn(true);
    manager = new DefaultZernovaSecurityManager();
  }

  @Test
  public void authenticateCorrectly()
      throws SecurityException, SecurityProviderNotFoundException {
    when(provider.authenticate(authentication)).thenReturn(authentication);
    manager.addProvider(provider);

    Optional<Authentication> optional = ofNullable(manager.authenticate(authentication));

    verify(provider).authenticate(authentication);

    assertThat(optional.get(), is(authentication));
  }

  @Test
  public void authenticateWithWrongAuthentication()
      throws SecurityException, SecurityProviderNotFoundException {
    when(provider.authenticate(authentication)).thenThrow(UnauthorisedException.class);
    manager.addProvider(provider);

    expected.expect(UnauthorisedException.class);

    try {
      manager.authenticate(authentication);
    } finally {
      verify(provider).authenticate(authentication);
    }
  }

  @Test
  public void authenticateWithFailedProvider()
      throws SecurityException, SecurityProviderNotFoundException {
    when(provider.authenticate(authentication)).thenThrow(SecurityException.class);
    manager.addProvider(provider);

    expected.expect(SecurityException.class);

    try {
      manager.authenticate(authentication);
    } finally {
      verify(provider).authenticate(authentication);
    }
  }

  @Test
  public void authenticateWithoutProvider()
      throws SecurityException, SecurityProviderNotFoundException {
    expected.expect(SecurityProviderNotFoundException.class);

    try {
      manager.authenticate(authentication);
    } finally {
      verifyNoInteractions(provider);
    }
  }

}
