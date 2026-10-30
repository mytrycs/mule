/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.internal;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.module.deployment.api.DeploymentListener;
import com.zernova.runtime.module.deployment.impl.internal.artifact.ZernovaContextListenerFactory;

/**
 * Creates {@link ZernovaContextListener} instances to tied the mule context lifecycle to the deployment lifecycle.
 */
public class DeploymentZernovaContextListenerFactory implements ZernovaContextListenerFactory {

  private final DeploymentListener deploymentListener;

  /**
   * Creates a new factory to link created contexts with a given deployment listener
   *
   * @param deploymentListener deployment listener to be notified for each context lifecycle notification. Non null.
   */
  public DeploymentZernovaContextListenerFactory(DeploymentListener deploymentListener) {
    checkArgument(deploymentListener != null, "deploymentListener cannot be null");

    this.deploymentListener = deploymentListener;
  }

  @Override
  public ZernovaContextListener create(String artifactName) {
    checkArgument(!isEmpty(artifactName), "artifactName cannot be empty");

    return new ZernovaContextDeploymentListener(artifactName, deploymentListener);
  }
}
