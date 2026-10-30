/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.descriptor;

import com.zernova.runtime.api.artifact.ArtifactType;

/**
 * Loads the {@link BundleDescriptor} for Mule artifacts.
 * <p/>
 * Explicitly defined to enable definition of implementations using SPI.
 */
public interface BundleDescriptorLoader extends DescriptorLoader<BundleDescriptor> {

  @Override
  default boolean supportsArtifactType(ArtifactType artifactType) {
    return true;
  }

}
