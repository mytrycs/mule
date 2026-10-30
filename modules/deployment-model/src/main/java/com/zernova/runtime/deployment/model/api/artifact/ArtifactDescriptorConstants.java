/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.api.artifact;

import com.zernova.runtime.api.deployment.meta.AbstractZernovaArtifactModel;
import com.zernova.runtime.api.deployment.meta.ZernovaPluginModel;

/**
 * Constants to be consumed across modules to generate and consume a proper {@link AbstractZernovaArtifactModel} when working with
 * the {@link ZernovaPluginModel#getExtensionModelLoaderDescriptor()}.
 *
 * @since 4.0
 * @deprecated since 4.5.0 use {@link com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorConstants} instead
 */
@Deprecated
public class ArtifactDescriptorConstants extends com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorConstants {

  private ArtifactDescriptorConstants() {

  }
}
