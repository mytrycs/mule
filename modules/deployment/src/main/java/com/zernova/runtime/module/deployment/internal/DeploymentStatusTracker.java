/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.internal;

import com.zernova.runtime.module.deployment.api.DeploymentListener;

/**
 * Keeps track of the deployment status of all artifact in the Mule instance.
 */
public class DeploymentStatusTracker implements DeploymentListener {

  private ArtifactDeploymentStatusTracker applicationDeploymentStatusTracker = new ArtifactDeploymentStatusTracker();
  private ArtifactDeploymentStatusTracker domainDeploymentStatusTracker = new ArtifactDeploymentStatusTracker();

  public ArtifactDeploymentStatusTracker getApplicationDeploymentStatusTracker() {
    return applicationDeploymentStatusTracker;
  }

  public ArtifactDeploymentStatusTracker getDomainDeploymentStatusTracker() {
    return domainDeploymentStatusTracker;
  }
}
