/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metadata.api.cache;

import com.zernova.runtime.api.parameterization.ComponentParameterization;

import java.util.Optional;

public interface ComponentParameterizationMetadataCacheIdGenerator
    extends MetadataCacheIdGenerator<ComponentParameterization<?>> {

  /**
   * Calculates the {@link MetadataCacheId} required to identify the MetadataType associated to the parameter named
   * {@code parameterName} of the given {@code component}. This method will take into account the values of the configured
   * {@link com.zernova.runtime.api.metadata.MetadataKey} to provide an unique identifier of the {@code component} attributes type
   * definition.
   *
   * @param parameterization   the component parametrization to calculate the metadata key from.
   * @param parameterGroupName the name of the parameter group
   * @param parameterName      the name of the parameter
   * @return a {@link MetadataCacheId} that identifies the component's parameter type with all its current configuration or
   *         {@link Optional#empty} if no valid identifier can be created.
   */
  Optional<MetadataCacheId> getIdForComponentInputMetadata(ComponentParameterization<?> parameterization,
                                                           String parameterGroupName,
                                                           String parameterName);

}
