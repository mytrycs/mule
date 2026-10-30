/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal;

import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServices;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.config.custom.CustomizationService;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.module.deployment.api.DeploymentListener;
import com.zernova.runtime.module.deployment.internal.ZernovaContextDeploymentListener;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class ZernovaContextDeploymentListenerTestCase extends AbstractZernovaTestCase {

  private static final String APP_NAME = "app";

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  private final ZernovaContext muleContext = mockContextWithServices();

  @Mock
  private Registry registry;

  @Mock
  private CustomizationService customizationService;

  private final DeploymentListener deploymentListener = mock(DeploymentListener.class);
  private final ZernovaContextDeploymentListener contextListener = new ZernovaContextDeploymentListener(APP_NAME, deploymentListener);

  @Before
  public void before() {
    when(muleContext.getCustomizationService()).thenReturn(customizationService);
  }

  @Test
  public void notifiesMuleContextCreated() throws Exception {
    contextListener.onCreation(muleContext);
    verify(deploymentListener).onArtifactCreated(APP_NAME, customizationService);
  }

  @Test
  public void notifiesMuleContextInitialized() throws Exception {
    contextListener.onInitialization(muleContext, registry);

    verify(deploymentListener).onArtifactInitialised(APP_NAME, registry);
  }

  @Test
  public void notifiesMuleContextStart() throws Exception {
    contextListener.onStart(muleContext, registry);

    verify(deploymentListener).onArtifactStarted(APP_NAME, registry);
  }

  @Test
  public void notifiesMuleContextStop() throws Exception {
    contextListener.onStop(muleContext, registry);

    verify(deploymentListener).onArtifactStopped(APP_NAME, registry);
  }
}
