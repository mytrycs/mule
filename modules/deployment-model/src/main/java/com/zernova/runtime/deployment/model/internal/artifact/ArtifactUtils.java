/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.internal.artifact;

import static java.lang.String.format;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;

/**
 * Helper methods for artifacts.
 *
 * @since 4.1
 */
public class ArtifactUtils {

  /**
   * Given a {@link BundleDescriptor} for a deployable artifact, it generates the artifact name.
   * <p/>
   * The artifact name will be unique for the each kind of artifact.
   * <p/>
   * The artifact name gets resolved based on the version of the artifact. {@link BundleDescriptor}s that only differ on the patch
   * version will generate the same artifact name.
   *
   * @param bundleDescriptor the bundle descriptor of the deployable archive
   * @return a unique artifact name within the artifact kind (app or domain).
   */
  public static String generateArtifactName(BundleDescriptor bundleDescriptor) {
    ZernovaVersion artifactVersion = new ZernovaVersion(bundleDescriptor.getVersion());
    return format("%s-%s-%s.%s", bundleDescriptor.getGroupId(), bundleDescriptor.getArtifactId(), artifactVersion.getMajor(),
                  artifactVersion.getMinor());
  }

}
