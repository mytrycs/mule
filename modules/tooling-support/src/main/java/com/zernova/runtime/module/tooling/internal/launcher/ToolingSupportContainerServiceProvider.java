/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.launcher;

import com.zernova.runtime.container.api.ZernovaCoreExtension;
import com.zernova.runtime.module.deployment.api.DeploymentService;
import com.zernova.runtime.module.deployment.impl.internal.ZernovaArtifactResourcesRegistry;
import com.zernova.runtime.module.launcher.privileged.ContainerServiceProvider;
import com.zernova.runtime.module.tooling.api.ToolingService;
import com.zernova.runtime.module.tooling.api.ToolingServiceAware;
import com.zernova.runtime.module.tooling.internal.DefaultToolingService;

/**
 * Registers a {@link ToolingService} as a container service.
 *
 * @since 4.10
 */
public class ToolingSupportContainerServiceProvider implements ContainerServiceProvider<ToolingService> {

  @Override
  public Class getServiceInterface() {
    return ToolingService.class;
  }

  @Override
  public ToolingService getServiceImplementation(DeploymentService deploymentService,
                                                 ZernovaArtifactResourcesRegistry artifactResourcesRegistry) {
    return new DefaultToolingService(artifactResourcesRegistry.getDomainRepository(),
                                     artifactResourcesRegistry.getDomainFactory(),
                                     artifactResourcesRegistry.getApplicationFactory(),
                                     artifactResourcesRegistry.getToolingApplicationDescriptorFactory());
  }

  @Override
  public void inject(ZernovaCoreExtension extension, ToolingService toolingService) {
    if (extension instanceof ToolingServiceAware tsaExtension) {
      tsaExtension.setToolingService(toolingService);
    }
  }

}
