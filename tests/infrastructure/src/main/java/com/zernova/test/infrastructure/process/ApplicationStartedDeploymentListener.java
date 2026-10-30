/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.infrastructure.process;

import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.module.deployment.api.DeploymentListener;

import java.util.concurrent.TimeUnit;

public class ApplicationStartedDeploymentListener implements DeploymentListener {

  public static final int APPLICATION_STARTED_TIMEOUT_IN_SECONDS = 20;

  private Latch applicationStartedLatch = new Latch();

  public void waitUntilApplicationDeployed() {
    try {
      if (!applicationStartedLatch.await(APPLICATION_STARTED_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)) {
        throw new RuntimeException("Application didn't start within " + APPLICATION_STARTED_TIMEOUT_IN_SECONDS + " seconds");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onDeploymentStart(String artifactName) {}

  @Override
  public void onDeploymentSuccess(String artifactName) {
    if (artifactName.equals(ZernovaContextProcessApplication.TEST_APPLICATION_NAME)) {
      applicationStartedLatch.release();
    }
  }
}
