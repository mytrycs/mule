/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.internal.descriptor;

import static java.util.Objects.requireNonNull;

import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidator;

import java.util.List;

/**
 * A composite implementation that executes a list of {@link ArtifactDescriptorValidator validators}.
 *
 * @since 4.1
 */
public class CompositeArtifactDescriptorValidator implements ArtifactDescriptorValidator {

  private List<ArtifactDescriptorValidator> validators;

  /**
   * Creates an instance of this validator.
   *
   * @param validators {@link List} of {@link ArtifactDescriptorValidator validators} to be called. Non null.
   */
  public CompositeArtifactDescriptorValidator(List<ArtifactDescriptorValidator> validators) {
    requireNonNull(validators, "validators cannot be null");
    this.validators = validators;
  }

  @Override
  public void validate(ArtifactDescriptor descriptor) {
    validators.stream().forEach(validator -> validator.validate(descriptor));
  }

}
