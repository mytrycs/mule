/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.model.parameter;

import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.deprecated.DeprecableModel;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.stereotype.HasStereotypeModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.api.meta.model.version.HasMinZernovaVersion;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Base class for immutable implementation of {@link HasStereotypeModel}
 *
 * @since 1.0
 */
public abstract class AbstractStereotypedModel extends AbstractParameterizedModel
    implements HasStereotypeModel, DeprecableModel, HasMinZernovaVersion {

  private final StereotypeModel stereotype;
  private final DeprecationModel deprecationModel;
  private final ZernovaVersion minMuleVersion;

  public AbstractStereotypedModel(String name, String description,
                                  List<ParameterGroupModel> parameterGroupModels,
                                  DisplayModel displayModel,
                                  StereotypeModel stereotype,
                                  Set<ModelProperty> modelProperties) {
    this(name, description, parameterGroupModels, displayModel, stereotype, modelProperties, null);
  }

  public AbstractStereotypedModel(String name, String description,
                                  List<ParameterGroupModel> parameterGroupModels,
                                  DisplayModel displayModel,
                                  StereotypeModel stereotype,
                                  Set<ModelProperty> modelProperties,
                                  DeprecationModel deprecationModel) {
    this(name, description, parameterGroupModels, displayModel, stereotype, modelProperties, deprecationModel, null);
  }

  public AbstractStereotypedModel(String name, String description,
                                  List<ParameterGroupModel> parameterGroupModels,
                                  DisplayModel displayModel,
                                  StereotypeModel stereotype,
                                  Set<ModelProperty> modelProperties,
                                  DeprecationModel deprecationModel,
                                  ZernovaVersion minMuleVersion) {
    super(name, description, parameterGroupModels, displayModel, modelProperties);
    this.stereotype = stereotype;
    this.deprecationModel = deprecationModel;
    this.minMuleVersion = minMuleVersion;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StereotypeModel getStereotype() {
    return stereotype;
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
    return "AbstractStereotypedModel{" +
        "stereotype=" + stereotype +
        ", deprecationModel=" + deprecationModel +
        ", minMuleVersion=" + minMuleVersion +
        "} " + super.toString();
  }
}
