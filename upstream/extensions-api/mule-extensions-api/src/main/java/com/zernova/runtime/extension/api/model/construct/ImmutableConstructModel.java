/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.model.construct;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ComponentVisibility;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.construct.ConstructModel;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.error.ErrorModel;
import com.zernova.runtime.api.meta.model.nested.NestableElementModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.extension.api.model.AbstractComponentModel;

import java.util.List;
import java.util.Set;

/**
 * Immutable concrete implementation of {@link OperationModel}
 *
 * @since 1.0
 */
public class ImmutableConstructModel extends AbstractComponentModel implements ConstructModel {

  private final boolean allowsTopLevelDefinition;

  /**
   * Creates a new instance with the given state
   *
   * @param name                     the operation's name. Cannot be blank
   * @param description              the operation's descriptor
   * @param parameterGroupModels     a {@link List} with the operation's {@link ParameterGroupModel parameter group models}
   * @param nestedComponents         a {@link List} with the components contained by this model
   * @param allowsTopLevelDefinition whether or not {@code this} model can be declared as a root component in the application
   * @param displayModel             a model which contains directive about how this operation is displayed in the UI
   * @param stereotype               the {@link StereotypeModel stereotype} of this component
   * @param visibility               the model's {@link ComponentVisibility}
   * @param modelProperties          A {@link Set} of custom properties which extend this model
   * @throws IllegalArgumentException if {@code name} is blank or {@code executorFactory} is {@code null}
   */
  public ImmutableConstructModel(String name,
                                 String description,
                                 List<ParameterGroupModel> parameterGroupModels,
                                 List<? extends NestableElementModel> nestedComponents,
                                 boolean allowsTopLevelDefinition,
                                 DisplayModel displayModel,
                                 Set<ErrorModel> errors,
                                 StereotypeModel stereotype,
                                 ComponentVisibility visibility,
                                 Set<ModelProperty> modelProperties) {
    this(name, description, parameterGroupModels, nestedComponents, allowsTopLevelDefinition, displayModel, errors, stereotype,
         visibility, modelProperties, null);
  }

  /**
   * Creates a new instance with the given state
   *
   * @param name                     the operation's name. Cannot be blank
   * @param description              the operation's descriptor
   * @param parameterGroupModels     a {@link List} with the operation's {@link ParameterGroupModel parameter group models}
   * @param nestedComponents         a {@link List} with the components contained by this model
   * @param allowsTopLevelDefinition whether or not {@code this} model can be declared as a root component in the application
   * @param displayModel             a model which contains directive about how this operation is displayed in the UI
   * @param stereotype               the {@link StereotypeModel stereotype} of this component
   * @param visibility               the model's {@link ComponentVisibility}
   * @param modelProperties          A {@link Set} of custom properties which extend this model
   * @param deprecationModel         a {@link DeprecationModel} describing if the construct is deprecated. A null value means it
   *                                 is not deprecated.
   * @throws IllegalArgumentException if {@code name} is blank or {@code executorFactory} is {@code null}
   */
  public ImmutableConstructModel(String name,
                                 String description,
                                 List<ParameterGroupModel> parameterGroupModels,
                                 List<? extends NestableElementModel> nestedComponents,
                                 boolean allowsTopLevelDefinition,
                                 DisplayModel displayModel,
                                 Set<ErrorModel> errors,
                                 StereotypeModel stereotype,
                                 ComponentVisibility visibility,
                                 Set<ModelProperty> modelProperties,
                                 DeprecationModel deprecationModel) {
    this(name, description, parameterGroupModels, nestedComponents, allowsTopLevelDefinition, displayModel, errors, stereotype,
         visibility, modelProperties,
         deprecationModel, null);
  }

  /**
   * Creates a new instance with the given state
   *
   * @param name                     the operation's name. Cannot be blank
   * @param description              the operation's descriptor
   * @param parameterGroupModels     a {@link List} with the operation's {@link ParameterGroupModel parameter group models}
   * @param nestedComponents         a {@link List} with the components contained by this model
   * @param allowsTopLevelDefinition whether or not {@code this} model can be declared as a root component in the application
   * @param displayModel             a model which contains directive about how this operation is displayed in the UI
   * @param stereotype               the {@link StereotypeModel stereotype} of this component
   * @param visibility               the model's {@link ComponentVisibility}
   * @param modelProperties          A {@link Set} of custom properties which extend this model
   * @param deprecationModel         a {@link DeprecationModel} describing if the construct is deprecated. A null value means it
   *                                 is not deprecated.
   * @throws IllegalArgumentException if {@code name} is blank or {@code executorFactory} is {@code null}
   */
  public ImmutableConstructModel(String name,
                                 String description,
                                 List<ParameterGroupModel> parameterGroupModels,
                                 List<? extends NestableElementModel> nestedComponents,
                                 boolean allowsTopLevelDefinition,
                                 DisplayModel displayModel,
                                 Set<ErrorModel> errors,
                                 StereotypeModel stereotype,
                                 ComponentVisibility visibility,
                                 Set<ModelProperty> modelProperties,
                                 DeprecationModel deprecationModel,
                                 Set<String> semanticTerms) {
    this(name, description, parameterGroupModels, nestedComponents, allowsTopLevelDefinition, displayModel, errors, stereotype,
         visibility, modelProperties, deprecationModel, semanticTerms, null);
  }

  /**
   * Creates a new instance with the given state
   *
   * @param name                     the operation's name. Cannot be blank
   * @param description              the operation's descriptor
   * @param parameterGroupModels     a {@link List} with the operation's {@link ParameterGroupModel parameter group models}
   * @param nestedComponents         a {@link List} with the components contained by this model
   * @param allowsTopLevelDefinition whether or not {@code this} model can be declared as a root component in the application
   * @param displayModel             a model which contains directive about how this operation is displayed in the UI
   * @param stereotype               the {@link StereotypeModel stereotype} of this component
   * @param visibility               the model's {@link ComponentVisibility}
   * @param modelProperties          A {@link Set} of custom properties which extend this model
   * @param deprecationModel         a {@link DeprecationModel} describing if the construct is deprecated. A null value means it
   *                                 is not deprecated.
   * @param minZernovaVersion           the min mule version of the construct model.
   * @throws IllegalArgumentException if {@code name} is blank or {@code executorFactory} is {@code null}
   */
  public ImmutableConstructModel(String name,
                                 String description,
                                 List<ParameterGroupModel> parameterGroupModels,
                                 List<? extends NestableElementModel> nestedComponents,
                                 boolean allowsTopLevelDefinition,
                                 DisplayModel displayModel,
                                 Set<ErrorModel> errors,
                                 StereotypeModel stereotype,
                                 ComponentVisibility visibility,
                                 Set<ModelProperty> modelProperties,
                                 DeprecationModel deprecationModel,
                                 Set<String> semanticTerms,
                                 ZernovaVersion minZernovaVersion) {
    super(name, description, parameterGroupModels, nestedComponents, displayModel, errors, stereotype, visibility,
          modelProperties,
          deprecationModel, semanticTerms, minZernovaVersion);
    this.allowsTopLevelDefinition = allowsTopLevelDefinition;
  }

  @Override
  public boolean allowsTopLevelDeclaration() {
    return allowsTopLevelDefinition;
  }

  @Override
  public String toString() {
    return "ImmutableConstructModel{" +
        "allowsTopLevelDefinition=" + allowsTopLevelDefinition +
        "} " + super.toString();
  }
}
