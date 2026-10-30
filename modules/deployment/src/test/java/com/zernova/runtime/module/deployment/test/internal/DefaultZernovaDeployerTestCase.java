/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.deployment.model.api.DeploymentException;
import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.module.deployment.internal.DefaultArtifactDeployer;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;

@SmallTest
public class DefaultZernovaDeployerTestCase extends AbstractZernovaTestCase {

  @Test
  public void disposesAppOnDeployFailure() throws Exception {
    DefaultArtifactDeployer deployer = new DefaultArtifactDeployer(() -> mock(SchedulerService.class));
    Application app = mock(Application.class);
    doThrow(new IllegalStateException()).when(app).init();

    try {
      deployer.deploy(app);
      fail("Deployment is supposed to fail");
    } catch (DeploymentException expected) {
    }

    verify(app, times(1)).dispose();
  }
}
