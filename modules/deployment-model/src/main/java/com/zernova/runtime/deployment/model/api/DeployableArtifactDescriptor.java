/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.api;

import java.util.Optional;
import java.util.Properties;

/**
 * Describes an artifact that is deployable on the container
 *
 * @deprecated From 4.5, use com.zernova.runtime.module.artifact.api.descriptor.DeployableArtifactDescriptor instead.
 */
@Deprecated
public class DeployableArtifactDescriptor extends com.zernova.runtime.module.artifact.api.descriptor.DeployableArtifactDescriptor {

  /**
   * Creates a new deployable artifact descriptor
   *
   * @param name artifact name. Non empty.
   */
  public DeployableArtifactDescriptor(String name) {
    super(name);
  }

  public DeployableArtifactDescriptor(String name, Optional<Properties> properties) {
    super(name, properties);
  }

}
