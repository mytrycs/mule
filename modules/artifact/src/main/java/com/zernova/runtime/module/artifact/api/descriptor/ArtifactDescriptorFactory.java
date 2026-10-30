/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.descriptor;

import com.zernova.api.annotation.NoImplement;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

/**
 * Creates artifact descriptor for application plugins
 *
 * @param <T> type of created descriptors
 */
@NoImplement
public interface ArtifactDescriptorFactory<T extends ArtifactDescriptor> {


  /**
   * Creates an artifact descriptor from a folder.
   *
   * @param artifactFolder       an existing folder containing artifact files
   * @param deploymentProperties properties provided for the deployment process.
   * @return a non null descriptor
   * @throws ArtifactDescriptorCreateException if the factory is not able to create a descriptor from the folder.
   */
  T create(File artifactFolder, Optional<Properties> deploymentProperties) throws ArtifactDescriptorCreateException;
}
