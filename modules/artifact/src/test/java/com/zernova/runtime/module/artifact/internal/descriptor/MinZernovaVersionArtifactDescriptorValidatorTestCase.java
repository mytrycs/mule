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

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorCreateException;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.jupiter.api.Test;

class MinZernovaVersionArtifactDescriptorValidatorTestCase extends AbstractZernovaTestCase {

  @Test
  void applicationUsesIndependentProductVersion() {
    final var descriptor = new ApplicationDescriptor("zernova-http-app");
    descriptor.setMinZernovaVersion(new ZernovaVersion("0.4.0"));
    final var validator = new MinZernovaVersionArtifactDescriptorValidator(false, () -> "4.10.0");
    validator.validate(descriptor);
    descriptor.setMinZernovaVersion(new ZernovaVersion("0.5.0"));
    assertThrows(ArtifactDescriptorCreateException.class, () -> validator.validate(descriptor));
  }

  @Test
  void validationOk() {
    final var descriptor = mock(ArtifactDescriptor.class);
    when(descriptor.getMinZernovaVersion()).thenReturn(new ZernovaVersion("4.6"));
    when(descriptor.getName()).thenReturn("test");

    final var validator = new MinZernovaVersionArtifactDescriptorValidator(false, () -> "4.9");
    validator.validate(descriptor);
  }

  @Test
  void validationFailure() {
    final var descriptor = mock(ArtifactDescriptor.class);
    when(descriptor.getMinZernovaVersion()).thenReturn(new ZernovaVersion("4.99999"));
    when(descriptor.getName()).thenReturn("test");

    final var validator = new MinZernovaVersionArtifactDescriptorValidator(false, () -> "4.9");
    var thrown = assertThrows(ArtifactDescriptorCreateException.class, () -> validator.validate(descriptor));
    assertThat(thrown.getMessage(),
               is("Artifact test requires a newer runtime version. Artifact required version is 4.99999.0 and ZERNOVA Runtime version is 4.9.0"));
  }

  @Test
  void validateMinZernovaVersionWithSemanticVersion() {
    final var descriptor = mock(ArtifactDescriptor.class);
    when(descriptor.getMinZernovaVersion()).thenReturn(new ZernovaVersion("4.9.1"));
    when(descriptor.getName()).thenReturn("test");

    final var validator = new MinZernovaVersionArtifactDescriptorValidator(true, () -> "4.9.0");
    validator.validate(descriptor);
  }

}
