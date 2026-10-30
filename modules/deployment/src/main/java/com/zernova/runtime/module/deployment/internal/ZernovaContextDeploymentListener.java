/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.internal;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.module.deployment.api.DeploymentListener;

/**
 * Delegates {@link ZernovaContextListener} notifications to a {@link DeploymentListener}
 */
public class ZernovaContextDeploymentListener implements ZernovaContextListener {

  private final String appName;
  private final DeploymentListener deploymentListener;

  public ZernovaContextDeploymentListener(String appName, DeploymentListener deploymentListener) {
    this.appName = appName;
    this.deploymentListener = deploymentListener;
  }

  @Override
  public void onCreation(ZernovaContext context) {
    deploymentListener.onArtifactCreated(appName, context.getCustomizationService());
  }

  @Override
  public void onInitialization(ZernovaContext context, Registry registry) {
    deploymentListener.onArtifactInitialised(appName, registry);
  }

  @Override
  public void onStart(ZernovaContext context, Registry registry) {
    deploymentListener.onArtifactStarted(appName, registry);
  }

  @Override
  public void onStop(ZernovaContext context, Registry registry) {
    deploymentListener.onArtifactStopped(appName, registry);
  }
}
