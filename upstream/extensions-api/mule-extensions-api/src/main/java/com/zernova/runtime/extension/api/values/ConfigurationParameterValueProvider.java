/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.values;

import static java.util.Collections.emptyList;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.model.parameter.ValueProviderModel;
import com.zernova.runtime.api.value.Value;
import com.zernova.sdk.api.annotation.values.FieldValues;

import java.util.List;
import java.util.Set;

/**
 * This interface allows the exposure of the {@link Set} of {@link Value values} associated to a Configuration's or Connection
 * Provider's parameter.
 *
 * @since 1.0
 */
@NoImplement
public interface ConfigurationParameterValueProvider {

  /**
   * Resolves the possible {@link Value values} for the Configuration's parameter identified by the {@code parameterName}
   *
   * @param parameterName the name of the parameter that has the capability to provide {@link Value values}
   * @return {@link Set} of possible and valid {@link Value values}
   */
  Set<Value> getConfigValues(String parameterName) throws ValueResolvingException;

  /**
   * Gets the values for the Configuration's parameter of the given {@code parameterName}, using the given {@code targetSelector}.
   * See {@link FieldValues#targetSelectors}
   *
   * @param parameterName  the name of the parameter for which its {@link Value values} will be resolved
   * @param targetSelector the target selector to use to resolve the {@link Value values}
   * @return {@link Set} of possible and valid {@link Value values}
   */
  Set<Value> getConfigValues(String parameterName, String targetSelector) throws ValueResolvingException;

  /**
   * Retrieves the list of associated {@link ValueProviderModel} with the given provider name in the configuration
   *
   * @param providerName The name of the value provider
   * @return The associated {@link ValueProviderModel}
   */
  @Deprecated
  default List<ValueProviderModel> getConfigModels(String providerName) throws ValueResolvingException {
    return emptyList();
  }

  /**
   * Resolves the possible {@link Value values} for the Connection Providers's parameter identified by the {@code parameterName}
   *
   * @param parameterName the name of the parameter that has the capability to provide {@link Value values}
   * @return {@link Set} of possible and valid {@link Value values}
   */
  Set<Value> getConnectionValues(String parameterName) throws ValueResolvingException;

  /**
   * Gets the values for the Connection Provider's parameter of the given {@code parameterName}, using the given
   * {@code targetSelector}. See {@link FieldValues#targetSelectors}
   *
   * @param parameterName  the name of the parameter for which its {@link Value values} will be resolved
   * @param targetSelector the target selector to use to resolve the {@link Value values}
   * @return {@link Set} of possible and valid {@link Value values}
   */
  Set<Value> getConnectionValues(String parameterName, String targetSelector) throws ValueResolvingException;

  /**
   * Retrieves the list of associated {@link ValueProviderModel} with the given provider name in the connection
   *
   * @param providerName The name of the value provider
   * @return The associated {@link ValueProviderModel}
   */
  @Deprecated
  default List<ValueProviderModel> getConnectionModels(String providerName) throws ValueResolvingException {
    return emptyList();
  }

}
