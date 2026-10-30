/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.internal;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.SINGLE_APP_MODE_PROPERTY;
import static com.zernova.runtime.module.deployment.internal.singleapp.SingleAppApplicationDeployerBuilder.getSingleAppApplicationDeployerBuilder;
import static com.zernova.runtime.module.deployment.internal.singleapp.SingleAppDomainDeployerBuilder.getSingleAppDomainDeployerBuilder;

import static java.lang.Boolean.getBoolean;
import static java.util.Objects.requireNonNull;

import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.module.deployment.api.DeploymentService;
import com.zernova.runtime.module.deployment.impl.internal.application.DefaultApplicationFactory;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainFactory;
import com.zernova.runtime.module.deployment.internal.singleapp.SingleAppApplicationDeployerBuilder;
import com.zernova.runtime.module.deployment.internal.singleapp.SingleAppDeploymentFileResolver;
import com.zernova.runtime.module.deployment.internal.singleapp.SingleAppDeploymentService;
import com.zernova.runtime.module.deployment.internal.singleapp.SingleAppDomainDeployerBuilder;
import com.zernova.runtime.module.deployment.internal.singleapp.WriteToRouteTerminationHandler;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A builder for {@link DeploymentService}.
 *
 * @since 4.7.0
 */
public class DeploymentServiceBuilder {

  public static final String DOMAIN_FACTORY_IS_NULL_ERROR_MESSAGE = "Domain Factory is null";
  public static final String APPLICATION_FACTORY_IS_NULL_ERROR_MESSAGE = "Application Factory is null";
  public static final String ARTIFACT_START_EXECUTOR_SUPPLIER_IS_NULL_ERROR_MESSAGE = "Artifact Start Executor Supplier is null";
  private DefaultDomainFactory domainFactory;
  private DefaultApplicationFactory applicationFactory;
  private Supplier<SchedulerService> artifactStartExecutorSupplier;
  private Consumer<Throwable> deploymentFailureThrowableConsumer = t -> {
  };

  private DeploymentServiceBuilder() {}

  public static DeploymentServiceBuilder deploymentServiceBuilder() {
    return new DeploymentServiceBuilder();
  }

  /**
   * @param domainFactory the {@link DefaultDomainFactory} to be used in the deployment service
   * @return the builder.
   */
  public DeploymentServiceBuilder withDomainFactory(DefaultDomainFactory domainFactory) {
    this.domainFactory = domainFactory;
    return this;
  }

  /**
   * @param applicationFactory the {@link DefaultApplicationFactory} to be used in the deployment service
   * @return the builder.
   */
  public DeploymentServiceBuilder withApplicationFactory(DefaultApplicationFactory applicationFactory) {
    this.applicationFactory = applicationFactory;
    return this;
  }

  /**
   * @param artifactStartExecutorSupplier the {@link Supplier<SchedulerService>} to be used in the deployment service
   * @return the builder.
   */
  public DeploymentServiceBuilder withArtifactStartExecutorSupplier(Supplier<SchedulerService> artifactStartExecutorSupplier) {
    this.artifactStartExecutorSupplier = artifactStartExecutorSupplier;
    return this;
  }

  /**
   * @param deploymentFailureThrowableConsumer the {@link Consumer<Throwable>} a consumer for the throwable in case of a
   *                                           deployment failure .
   * @return the builder.
   */
  public DeploymentServiceBuilder withDeploymentFailureThrowableConsumer(Consumer<Throwable> deploymentFailureThrowableConsumer) {
    this.deploymentFailureThrowableConsumer = deploymentFailureThrowableConsumer;
    return this;
  }

  public DeploymentService build() {
    requireNonNull(domainFactory, DOMAIN_FACTORY_IS_NULL_ERROR_MESSAGE);
    requireNonNull(applicationFactory, APPLICATION_FACTORY_IS_NULL_ERROR_MESSAGE);
    requireNonNull(artifactStartExecutorSupplier, ARTIFACT_START_EXECUTOR_SUPPLIER_IS_NULL_ERROR_MESSAGE);

    if (getBoolean(SINGLE_APP_MODE_PROPERTY)) {
      SingleAppDomainDeployerBuilder singleAppDomainDeployerBuilder =
          getSingleAppDomainDeployerBuilder(artifactStartExecutorSupplier);
      singleAppDomainDeployerBuilder
          .withDomainFactory(domainFactory)
          .withApplicationFactory(applicationFactory);

      SingleAppApplicationDeployerBuilder singleAppApplicationDeployerBuilder =
          getSingleAppApplicationDeployerBuilder(artifactStartExecutorSupplier);
      singleAppApplicationDeployerBuilder.withApplicationFactory(applicationFactory);


      DeploymentService deploymentService = new SingleAppDeploymentService(singleAppDomainDeployerBuilder,
                                                                           singleAppApplicationDeployerBuilder,
                                                                           new SingleAppDeploymentFileResolver(),
                                                                           new ArrayList<>(),
                                                                           new ArrayList<>(),
                                                                           artifactStartExecutorSupplier);

      deploymentService.onDeploymentError(new WriteToRouteTerminationHandler(deploymentFailureThrowableConsumer));

      return deploymentService;
    }

    return new ZernovaDeploymentService(domainFactory,
                                     applicationFactory,
                                     artifactStartExecutorSupplier);
  }
}
