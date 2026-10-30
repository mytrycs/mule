/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metadata.internal.types;

import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.meta.model.EnrichableModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.api.parameterization.ComponentParameterization;
import com.zernova.runtime.extension.api.property.ResolverInformation;
import com.zernova.runtime.extension.api.property.TypeResolversInformationModelProperty;
import com.zernova.runtime.metadata.api.dsl.DslElementModel;

import java.util.Optional;
import java.util.function.Function;

/**
 * Abstract implementation of {@link MetadataResolutionTypeInformation} that is based on a {@link ComponentAst}
 *
 * @since 4.2.0
 */
public abstract class AbstractMetadataResolutionTypeInformation implements MetadataResolutionTypeInformation {

  private String resolverCategory;
  private String resolverName;

  private void setPrivateFields(EnrichableModel enrichableModel,
                                Function<TypeResolversInformationModelProperty, Optional<ResolverInformation>> getResolverInformationFromModelProperty) {
    Optional<TypeResolversInformationModelProperty> typeResolversInformationModelProperty =
        enrichableModel.getModelProperty(TypeResolversInformationModelProperty.class);
    if (typeResolversInformationModelProperty.isPresent()) {
      resolverName = getResolverInformationFromModelProperty.apply(typeResolversInformationModelProperty.get())
          .map(resolverInformation -> resolverInformation.getResolverName()).orElse(null);
      resolverCategory = typeResolversInformationModelProperty.get().getCategoryName();
    }
  }

  public AbstractMetadataResolutionTypeInformation(ComponentAst component,
                                                   Function<TypeResolversInformationModelProperty, Optional<ResolverInformation>> getResolverInformationFromModelProperty) {
    component.getModel(EnrichableModel.class)
        .ifPresent(em -> setPrivateFields(em, getResolverInformationFromModelProperty));
  }

  public AbstractMetadataResolutionTypeInformation(DslElementModel<?> component,
                                                   Function<TypeResolversInformationModelProperty, Optional<ResolverInformation>> getResolverInformationFromModelProperty) {
    if (component.getModel() instanceof EnrichableModel) {
      this.setPrivateFields((EnrichableModel) component.getModel(), getResolverInformationFromModelProperty);
    }
  }

  public AbstractMetadataResolutionTypeInformation(ComponentParameterization parameterization,
                                                   Function<TypeResolversInformationModelProperty, Optional<ResolverInformation>> getResolverInformationFromModelProperty) {
    ParameterizedModel model = parameterization.getModel();
    if (model instanceof EnrichableModel) {
      setPrivateFields(((EnrichableModel) model), getResolverInformationFromModelProperty);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDynamicType() {
    return resolverCategory != null && resolverName != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getResolverName() {
    return ofNullable(resolverName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getResolverCategory() {
    return ofNullable(resolverCategory);
  }
}
