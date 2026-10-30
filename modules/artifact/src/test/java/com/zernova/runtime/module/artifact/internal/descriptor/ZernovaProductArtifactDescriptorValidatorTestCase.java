/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.internal.descriptor;

import static com.zernova.runtime.api.deployment.meta.Product.MULE;
import static com.zernova.runtime.api.deployment.meta.Product.MULE_EE;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorCreateException;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.jupiter.api.Test;

class ZernovaProductArtifactDescriptorValidatorTestCase extends AbstractZernovaTestCase {

  @Test
  void supportedProduct() {
    final var descriptor = mock(ArtifactDescriptor.class);
    when(descriptor.getRequiredProduct()).thenReturn(MULE);

    final var validator = new ZernovaProductArtifactDescriptorValidator();
    validator.validate(descriptor);
  }

  @Test
  void unsupportedProduct() {
    final var descriptor = mock(ArtifactDescriptor.class);
    when(descriptor.getRequiredProduct()).thenReturn(MULE_EE);

    final var validator = new ZernovaProductArtifactDescriptorValidator();
    var thrown = assertThrows(ArtifactDescriptorCreateException.class, () -> validator.validate(descriptor));
    assertThat(thrown.getMessage(),
               is("The artifact null requires a different runtime. The artifact required runtime is MULE_EE and the runtime is MULE"));
  }

}
