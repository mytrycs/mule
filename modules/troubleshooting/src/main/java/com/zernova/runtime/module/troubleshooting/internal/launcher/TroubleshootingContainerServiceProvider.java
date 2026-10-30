/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.troubleshooting.internal.launcher;

import com.zernova.runtime.container.api.ZernovaCoreExtension;
import com.zernova.runtime.module.deployment.api.DeploymentService;
import com.zernova.runtime.module.deployment.impl.internal.ZernovaArtifactResourcesRegistry;
import com.zernova.runtime.module.launcher.privileged.ContainerServiceProvider;
import com.zernova.runtime.module.troubleshooting.api.TroubleshootingService;
import com.zernova.runtime.module.troubleshooting.api.TroubleshootingServiceAware;
import com.zernova.runtime.module.troubleshooting.internal.DefaultTroubleshootingService;

/**
 * Registers a {@link TroubleshootingService} as a container service.
 *
 * @since 4.10
 */
public class TroubleshootingContainerServiceProvider implements ContainerServiceProvider<TroubleshootingService> {

  @Override
  public Class<TroubleshootingService> getServiceInterface() {
    return TroubleshootingService.class;
  }

  @Override
  public TroubleshootingService getServiceImplementation(DeploymentService deploymentService,
                                                         ZernovaArtifactResourcesRegistry artifactResourcesRegistry) {
    return new DefaultTroubleshootingService(deploymentService);
  }

  @Override
  public void inject(ZernovaCoreExtension extension, TroubleshootingService troubleshootingService) {
    if (extension instanceof TroubleshootingServiceAware tsaExtension) {
      tsaExtension.setTroubleshootingService(troubleshootingService);
    }
  }

}
