/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.bean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.jupiter.api.Test;

public class ZernovaConfigurationDelegateTestCase extends AbstractZernovaTestCase {

  @Test
  void delegatesIdsProperly() {
    final var muleConfiguration = mock(ZernovaConfiguration.class);
    when(muleConfiguration.getId()).thenReturn("theAppId");
    when(muleConfiguration.getDomainId()).thenReturn("theDomainId");

    final var muleContext = mock(ZernovaContext.class);
    when(muleContext.getConfiguration()).thenReturn(muleConfiguration);

    final var muleConfigurationDelegate = new ZernovaConfigurationDelegate();
    muleConfigurationDelegate.setMuleContext(muleContext);
    assertThat(muleConfigurationDelegate.getId(), is("theAppId"));
    assertThat(muleConfigurationDelegate.getDomainId(), is("theDomainId"));
  }

  @Test
  void delegatesDirectoriesProperly() {
    final var muleConfiguration = mock(ZernovaConfiguration.class);
    when(muleConfiguration.getWorkingDirectory()).thenReturn("theWorkDir");
    when(muleConfiguration.getMuleHomeDirectory()).thenReturn("theHomeDir");

    final var muleContext = mock(ZernovaContext.class);
    when(muleContext.getConfiguration()).thenReturn(muleConfiguration);

    final var muleConfigurationDelegate = new ZernovaConfigurationDelegate();
    muleConfigurationDelegate.setMuleContext(muleContext);
    assertThat(muleConfigurationDelegate.getWorkingDirectory(), is("theWorkDir"));
    assertThat(muleConfigurationDelegate.getMuleHomeDirectory(), is("theHomeDir"));
  }

}
