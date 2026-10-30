/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.internal.descriptor;

import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorCreateException;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidator;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;

import java.util.function.Supplier;

/**
 * Validator for {@link ArtifactDescriptor#getMinZernovaVersion()}. By default compares versions using {@code major.minor.patch} but
 * can be configured to do the comparison using semantic version.
 *
 * @since 4.1
 */
public class MinZernovaVersionArtifactDescriptorValidator implements ArtifactDescriptorValidator {

  private boolean validateMinZernovaVersionWithSemanticVersion;
  private Supplier<String> muleRuntimeVersionSupplier = () -> getMuleManifest().getProductVersion();

  /**
   * Creates an instance of this validator.
   *
   * @param validateMinZernovaVersionWithSemanticVersion {@code true} uses semantic version for checking
   *                                                  {@link ArtifactDescriptor#getMinZernovaVersion()}.
   */
  public MinZernovaVersionArtifactDescriptorValidator(boolean validateMinZernovaVersionWithSemanticVersion) {
    this.validateMinZernovaVersionWithSemanticVersion = validateMinZernovaVersionWithSemanticVersion;
  }

  /**
   * Creates an instance of this validator.
   *
   * @param validateMinZernovaVersionWithSemanticVersion {@code true} uses semantic version for checking
   *                                                  {@link ArtifactDescriptor#getMinZernovaVersion()}.
   * @param muleRuntimeVersionSupplier                {@link Supplier} to get Mule Runtime version. Non null.
   */
  public MinZernovaVersionArtifactDescriptorValidator(boolean validateMinZernovaVersionWithSemanticVersion,
                                                   Supplier<String> muleRuntimeVersionSupplier) {
    this(validateMinZernovaVersionWithSemanticVersion);
    requireNonNull(muleRuntimeVersionSupplier, "muleRuntimeVersionSupplier cannot be null");

    this.muleRuntimeVersionSupplier = muleRuntimeVersionSupplier;
  }

  @Override
  public void validate(ArtifactDescriptor descriptor) {
    ZernovaVersion minZernovaVersion = descriptor.getMinZernovaVersion();
    ZernovaVersion runtimeVersion = new ZernovaVersion(muleRuntimeVersionSupplier.get());
    // Applications target the independent ZERNOVA product version. Adapted
    // connector metadata still declares its upstream SDK compatibility floor.
    if (descriptor instanceof ApplicationDescriptor) {
      runtimeVersion = new ZernovaVersion(System.getProperty("zernova.distribution.version", "0.4.0"));
    }
    runtimeVersion = new ZernovaVersion(runtimeVersion.toCompleteNumericVersion().replace("-" + runtimeVersion.getSuffix(), ""));
    if (validateMinZernovaVersionWithSemanticVersion) {
      minZernovaVersion = toBaseVersion(minZernovaVersion);
      runtimeVersion = toBaseVersion(runtimeVersion);
    }

    doValidation(descriptor, minZernovaVersion, runtimeVersion);
  }

  private ZernovaVersion toBaseVersion(ZernovaVersion version) {
    return new ZernovaVersion(version.getMajor() + "." + version.getMinor());
  }

  private void doValidation(ArtifactDescriptor descriptor, ZernovaVersion minZernovaVersion, ZernovaVersion runtimeVersion) {
    if (runtimeVersion.priorTo(minZernovaVersion)) {
      throw new ArtifactDescriptorCreateException(format("Artifact %s requires a newer runtime version. Artifact required version is %s and ZERNOVA Runtime version is %s",
                                                         descriptor.getName(),
                                                         descriptor.getMinZernovaVersion().toCompleteNumericVersion(),
                                                         runtimeVersion.toCompleteNumericVersion()));
    }
  }

}
