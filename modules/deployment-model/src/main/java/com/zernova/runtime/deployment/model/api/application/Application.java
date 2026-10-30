/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.api.application;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.core.api.data.sample.SampleDataService;
import com.zernova.runtime.deployment.model.api.DeployableArtifact;
import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.module.artifact.api.classloader.RegionOwnerArtifact;

@NoImplement
public interface Application
    extends DeployableArtifact<com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor>, RegionOwnerArtifact {

  /**
   * @return the domain associated with the application.
   */
  Domain getDomain();

  /**
   * @return the current status of the application
   */
  ApplicationStatus getStatus();

  /**
   * @return the policy manager for the application
   */
  ApplicationPolicyManager getPolicyManager();

  /**
   * Do not use this method if the artifact initialization wasn't successful or the application has been destroyed.
   *
   * @return the {@link SampleDataService} which can resolve possible values for a component configuration.
   */
  SampleDataService getSampleDataService();

}
