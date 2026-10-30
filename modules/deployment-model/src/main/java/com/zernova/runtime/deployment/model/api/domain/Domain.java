/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.api.domain;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.deployment.model.api.DeployableArtifact;
import com.zernova.runtime.deployment.model.api.application.Application;

/**
 * A domain is a deployable Artifact that contains shared resources for {@link Application}
 * <p/>
 * A domain can just consist of a set of jar libraries to share between the domain applications or it can also contain shared
 * resources such as connectors or other mule components.
 */
@NoImplement
public interface Domain extends DeployableArtifact<com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor> {

  /**
   * @return true if this domain has shared mule components, false if it doesn't
   */
  boolean containsSharedResources();

}
