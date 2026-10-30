/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.internal.descriptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorCreateException;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.jupiter.api.Test;

class VersionFormatArtifactDescriptorValidatorTestCase extends AbstractZernovaTestCase {

  @Test
  void nullBundleDescriptorVersion() {
    final var bundleDescriptor = mock(BundleDescriptor.class);
    when(bundleDescriptor.getVersion()).thenReturn(null);

    final var descriptor = mock(ArtifactDescriptor.class);
    when(descriptor.getBundleDescriptor()).thenReturn(bundleDescriptor);
    final var validator = new VersionFormatArtifactDescriptorValidator(false);

    var thrown = assertThrows(ArtifactDescriptorCreateException.class, () -> validator.validate(descriptor));
    assertThat(thrown.getMessage(), is("No version specified in the bundle descriptor of the artifact null"));
  }

  @Test
  void noRevisionBundleDescriptorVersion() {
    final var bundleDescriptor = mock(BundleDescriptor.class);
    when(bundleDescriptor.getVersion()).thenReturn("4.6");

    final var descriptor = mock(ArtifactDescriptor.class);
    when(descriptor.getBundleDescriptor()).thenReturn(bundleDescriptor);

    final var validator = new VersionFormatArtifactDescriptorValidator(false);
    var thrown = assertThrows(ArtifactDescriptorCreateException.class, () -> validator.validate(descriptor));
    assertThat(thrown.getMessage(),
               is("Artifact null version 4.6 must contain a revision number. The version format must be x.y.z and the z part is missing"));
  }

  @Test
  void validationSkipped() {
    final var descriptor = mock(ArtifactDescriptor.class);
    when(descriptor.getBundleDescriptor()).thenReturn(null);

    final var validator = new VersionFormatArtifactDescriptorValidator(true);
    validator.validate(descriptor);
  }

  @Test
  void validationNullBundleDescriptor() {
    final var descriptor = mock(ArtifactDescriptor.class);
    when(descriptor.getBundleDescriptor()).thenReturn(null);

    final var validator = new VersionFormatArtifactDescriptorValidator(false);
    assertThrows(NullPointerException.class, () -> validator.validate(descriptor));
  }

}
