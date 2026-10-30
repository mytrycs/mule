/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.model.function;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.OutputModel;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.function.FunctionModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.extension.api.model.parameter.AbstractParameterizedModel;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * Immutable concrete implementation of {@link FunctionModel}
 *
 * @since 1.0
 */
public class ImmutableFunctionModel extends AbstractParameterizedModel implements FunctionModel {

  private final OutputModel output;
  private final DeprecationModel deprecationModel;
  private final ZernovaVersion minMuleVersion;

  /**
   * Creates a new instance with the given state
   *
   * @param name                 the operation's name. Cannot be blank
   * @param description          the operation's descriptor
   * @param parameterGroupModels a {@link List} with the operation's {@link ParameterGroupModel parameter group models}
   * @param output               an {@link OutputModel} which represents the operation's output content
   * @param displayModel         a model which contains directive about how this operation is displayed in the UI
   * @param modelProperties      A {@link Set} of custom properties which extend this model
   * @throws IllegalArgumentException if {@code name} is blank or {@code executorFactory} is {@code null}
   */
  public ImmutableFunctionModel(String name,
                                String description,
                                List<ParameterGroupModel> parameterGroupModels,
                                OutputModel output,
                                DisplayModel displayModel,
                                Set<ModelProperty> modelProperties) {
    this(name, description, parameterGroupModels, output, displayModel, modelProperties, null);
  }

  /**
   * Creates a new instance with the given state
   *
   * @param name                 the operation's name. Cannot be blank
   * @param description          the operation's descriptor
   * @param parameterGroupModels a {@link List} with the operation's {@link ParameterGroupModel parameter group models}
   * @param output               an {@link OutputModel} which represents the operation's output content
   * @param displayModel         a model which contains directive about how this operation is displayed in the UI
   * @param modelProperties      A {@link Set} of custom properties which extend this model
   * @param deprecationModel     a {@link DeprecationModel} describing if the function is deprecated. A null value means it is not
   *                             deprecated.
   * @throws IllegalArgumentException if {@code name} is blank or {@code executorFactory} is {@code null}
   */
  public ImmutableFunctionModel(String name,
                                String description,
                                List<ParameterGroupModel> parameterGroupModels,
                                OutputModel output,
                                DisplayModel displayModel,
                                Set<ModelProperty> modelProperties,
                                DeprecationModel deprecationModel) {
    this(name, description, parameterGroupModels, output, displayModel, modelProperties, deprecationModel, null);
  }

  /**
   * Creates a new instance with the given state
   *
   * @param name                 the operation's name. Cannot be blank
   * @param description          the operation's descriptor
   * @param parameterGroupModels a {@link List} with the operation's {@link ParameterGroupModel parameter group models}
   * @param output               an {@link OutputModel} which represents the operation's output content
   * @param displayModel         a model which contains directive about how this operation is displayed in the UI
   * @param modelProperties      A {@link Set} of custom properties which extend this model
   * @param deprecationModel     a {@link DeprecationModel} describing if the function is deprecated. A null value means it is not
   *                             deprecated.
   * @param minMuleVersion       the min mule version of the function
   * @throws IllegalArgumentException if {@code name} is blank or {@code executorFactory} is {@code null}
   * @since 1.5
   */
  public ImmutableFunctionModel(String name,
                                String description,
                                List<ParameterGroupModel> parameterGroupModels,
                                OutputModel output,
                                DisplayModel displayModel,
                                Set<ModelProperty> modelProperties,
                                DeprecationModel deprecationModel,
                                ZernovaVersion minMuleVersion) {
    super(name, description, parameterGroupModels, displayModel, modelProperties);
    this.output = output;
    this.deprecationModel = deprecationModel;
    this.minMuleVersion = minMuleVersion;
  }

  @Override
  public OutputModel getOutput() {
    return output;
  }

  @Override
  public Optional<DeprecationModel> getDeprecationModel() {
    return ofNullable(deprecationModel);
  }

  @Override
  public boolean isDeprecated() {
    return deprecationModel != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<ZernovaVersion> getMinMuleVersion() {
    return ofNullable(minMuleVersion);
  }

  @Override
  public String toString() {
    return "ImmutableFunctionModel{" +
        "output=" + output +
        ", deprecationModel=" + deprecationModel +
        ", minMuleVersion=" + minMuleVersion +
        "} " + super.toString();
  }
}
