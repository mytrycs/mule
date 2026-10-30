/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.descriptor;

import com.zernova.api.annotation.NoImplement;

/**
 * {@link ArtifactDescriptorValidator} for {@link ArtifactDescriptor}.
 *
 * @since 4.1
 */
@NoImplement
public interface ArtifactDescriptorValidator {

  /**
   * Validates the {@link ArtifactDescriptor}. If validation fails a {@link com.zernova.runtime.api.exception.ZernovaRuntimeException}
   * would be thrown.
   *
   * @param descriptor an {@link ArtifactDescriptor} to be validated.
   * @throws ArtifactDescriptorCreateException
   */
  void validate(ArtifactDescriptor descriptor);

}
