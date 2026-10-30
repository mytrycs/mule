/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.internal.singleapp;

import static com.zernova.runtime.module.deployment.internal.ArtifactDeploymentTemplate.NOP_ARTIFACT_DEPLOYMENT_TEMPLATE;

import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.deployment.impl.internal.application.DefaultApplicationFactory;
import com.zernova.runtime.module.deployment.internal.CompositeDeploymentListener;
import com.zernova.runtime.module.deployment.internal.DefaultArchiveDeployer;
import com.zernova.runtime.module.deployment.internal.DefaultArtifactDeployer;
import com.zernova.runtime.module.deployment.internal.DeploymentZernovaContextListenerFactory;
import com.zernova.runtime.module.deployment.internal.util.ObservableList;

import java.util.List;
import java.util.function.Supplier;

/**
 * A builder for an application {@link DefaultArchiveDeployer} to be used in single app mode.
 */
public class SingleAppApplicationDeployerBuilder {

  private final Supplier<SchedulerService> artifactStartExecutorSupplier;
  private ObservableList<Application> applications;
  private DefaultApplicationFactory applicationFactory;
  private DefaultArtifactDeployer applicationDeployer;
  private CompositeDeploymentListener applicationDeploymentListener;

  private SingleAppApplicationDeployerBuilder(Supplier<SchedulerService> artifactStartExecutorSupplier) {
    this.artifactStartExecutorSupplier = artifactStartExecutorSupplier;
  }

  public static SingleAppApplicationDeployerBuilder getSingleAppApplicationDeployerBuilder(Supplier<SchedulerService> artifactStartExecutorSupplier) {
    return new SingleAppApplicationDeployerBuilder(artifactStartExecutorSupplier);
  }

  public DefaultArchiveDeployer<ApplicationDescriptor, Application> build() {
    return new DefaultArchiveDeployer<>(applicationDeployer, applicationFactory, applications,
                                        NOP_ARTIFACT_DEPLOYMENT_TEMPLATE,
                                        new DeploymentZernovaContextListenerFactory(applicationDeploymentListener),
                                        artifactStartExecutorSupplier);
  }

  public SingleAppApplicationDeployerBuilder withApplications(List<Application> applications) {
    this.applications = new ObservableList<>(applications);
    return this;
  }

  public SingleAppApplicationDeployerBuilder withApplicationFactory(DefaultApplicationFactory applicationFactory) {
    this.applicationFactory = applicationFactory;
    return this;
  }

  public SingleAppApplicationDeployerBuilder withApplicationDeployer(DefaultArtifactDeployer<Application> applicationDeployer) {
    this.applicationDeployer = applicationDeployer;
    return this;
  }

  public SingleAppApplicationDeployerBuilder withApplicationDeploymentListener(CompositeDeploymentListener applicationDeploymentListener) {
    this.applicationDeploymentListener = applicationDeploymentListener;
    return this;
  }
}
