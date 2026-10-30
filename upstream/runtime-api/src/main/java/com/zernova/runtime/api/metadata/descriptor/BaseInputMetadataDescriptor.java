/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.metadata.descriptor;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.message.Message;

import java.util.Map;

/**
 * Base class for input metadata descriptors.
 *
 * <b>NOTE:</b> This class is to ONLY be implemented by Mule Runtime. No customer or external component should extend it.
 *
 * @since 1.7.0
 */
@NoExtend
public abstract class BaseInputMetadataDescriptor {

  private final Map<String, ParameterMetadataDescriptor> parameters;

  protected BaseInputMetadataDescriptor(Map<String, ParameterMetadataDescriptor> parameters) {
    this.parameters = unmodifiableMap(parameters);
  }

  /**
   * @return a {@link TypeMetadataDescriptor} that describes the Component's output {@link Message#getPayload}
   */
  public ParameterMetadataDescriptor getParameterMetadata(String paramName) {
    ParameterMetadataDescriptor result = parameters.get(paramName);
    if (result == null) {
      throw new IllegalArgumentException(format("The parameter [%s] does not belong to the described component", paramName));
    }
    return result;
  }

  /**
   * @return a {@link TypeMetadataDescriptor} that describes the Component's output {@link Message#getAttributes}
   */
  public Map<String, ParameterMetadataDescriptor> getAllParameters() {
    return parameters;
  }

}
