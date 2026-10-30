/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.lifecycle;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.core.api.config.ZernovaDeploymentProperties.MULE_LAZY_INIT_DEPLOYMENT_PROPERTY;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.isLazyInitMode;

import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;

@SmallTest
public class LifecycleUtilsTestCase extends AbstractZernovaTestCase {

  @Test
  public void isLazyInit() {
    ConfigurationProperties configurationProperties = mock(ConfigurationProperties.class);

    when(configurationProperties.resolveBooleanProperty(MULE_LAZY_INIT_DEPLOYMENT_PROPERTY)).thenReturn(of(true));
    assertThat(isLazyInitMode(configurationProperties), is(true));

    when(configurationProperties.resolveBooleanProperty(MULE_LAZY_INIT_DEPLOYMENT_PROPERTY)).thenReturn(of(false));
    assertThat(isLazyInitMode(configurationProperties), is(false));

    when(configurationProperties.resolveBooleanProperty(MULE_LAZY_INIT_DEPLOYMENT_PROPERTY)).thenReturn(empty());
    assertThat(isLazyInitMode(configurationProperties), is(false));

    assertThat(isLazyInitMode(null), is(false));
  }
}
