/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.internal;

import static com.zernova.runtime.module.deployment.internal.ArtifactDeploymentStatusTracker.DeploymentState.DEPLOYED;
import static com.zernova.runtime.module.deployment.internal.ArtifactDeploymentStatusTracker.DeploymentState.DEPLOYING;
import static com.zernova.runtime.module.deployment.internal.ArtifactDeploymentStatusTracker.DeploymentState.FAILED;

import static java.util.Collections.unmodifiableMap;

import com.zernova.runtime.module.deployment.api.DeploymentListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of the deployment status of the artifact listening to the deployment notifications
 */
public class ArtifactDeploymentStatusTracker implements DeploymentListener {

  public enum DeploymentState {
    // The deployment is in progress
    DEPLOYING,
    // The deployment was finished with a failure
    FAILED,
    // The deployment was successfully finished
    DEPLOYED
  }

  protected Map<String, DeploymentState> deploymentStates = new ConcurrentHashMap<>();

  public Map<String, DeploymentState> getDeploymentStates() {
    return unmodifiableMap(deploymentStates);
  }

  @Override
  public void onDeploymentStart(String artifactName) {
    deploymentStates.put(artifactName, DEPLOYING);
  }

  @Override
  public void onDeploymentSuccess(String artifactName) {
    deploymentStates.put(artifactName, DEPLOYED);
  }

  @Override
  public void onDeploymentFailure(String artifactName, Throwable failureCause) {
    deploymentStates.put(artifactName, FAILED);
  }

}
