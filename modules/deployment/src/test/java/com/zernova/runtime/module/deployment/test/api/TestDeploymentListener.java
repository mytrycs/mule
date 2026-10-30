/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.api;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.module.deployment.api.DeploymentListener;

public class TestDeploymentListener implements DeploymentListener {

  private String artifactName;

  private Registry registry;

  @Override
  public void onArtifactInitialised(String artifactName, Registry registry) {
    this.artifactName = artifactName;
    this.registry = registry;
  }

  @Override
  public void onDeploymentSuccess(String artifactName) {
    this.artifactName = artifactName;
  }

  public String getArtifactName() {
    return artifactName;
  }

  public Registry getRegistry() {
    return registry;
  }
}
