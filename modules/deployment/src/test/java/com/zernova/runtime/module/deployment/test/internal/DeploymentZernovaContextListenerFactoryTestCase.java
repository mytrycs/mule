/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import com.zernova.runtime.api.config.custom.CustomizationService;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.module.deployment.api.DeploymentListener;
import com.zernova.runtime.module.deployment.internal.DeploymentZernovaContextListenerFactory;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class DeploymentZernovaContextListenerFactoryTestCase extends AbstractZernovaTestCase {

  public static final String APP_NAME = "app";
  private final DeploymentListener deploymentListener = mock(DeploymentListener.class);
  private final DeploymentZernovaContextListenerFactory factory = new DeploymentZernovaContextListenerFactory(deploymentListener);

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ZernovaContext muleContext;

  @Test
  public void createsContextListener() throws Exception {
    ZernovaContextListener contextListener = factory.create(APP_NAME);

    contextListener.onCreation(muleContext);

    verify(deploymentListener).onArtifactCreated(eq(APP_NAME), any(CustomizationService.class));
  }
}
