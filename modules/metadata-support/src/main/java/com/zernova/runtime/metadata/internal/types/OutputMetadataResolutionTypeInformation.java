/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.metadata.internal.types;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.api.meta.model.HasOutputModel;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.metadata.api.cache.MetadataCacheId;
import com.zernova.runtime.api.parameterization.ComponentParameterization;
import com.zernova.runtime.extension.api.property.ResolverInformation;
import com.zernova.runtime.extension.api.property.TypeResolversInformationModelProperty;
import com.zernova.runtime.metadata.api.dsl.DslElementModel;

import java.util.Optional;

/**
 * Implementation of {@link MetadataResolutionTypeInformation} that describes Output Types from a {@link ComponentAst}
 *
 * @since 4.2.0
 */
public class OutputMetadataResolutionTypeInformation extends AbstractMetadataResolutionTypeInformation {

  private static final String TYPE_IDENTIFIER = "Output";
  private static final MetadataCacheId COMPONENT_TYPE_METADATA_CACHE_ID =
      new MetadataCacheId(TYPE_IDENTIFIER.hashCode(), TYPE_IDENTIFIER);

  public OutputMetadataResolutionTypeInformation(ComponentAst component) {
    super(component, (typeResolversInformationModelProperty -> getResolverInformation(typeResolversInformationModelProperty)));
    checkArgument(component.getModel(HasOutputModel.class).isPresent(),
                  "Cannot generate an Output Cache Key for a component with no output");
  }

  public OutputMetadataResolutionTypeInformation(DslElementModel<?> component) {
    super(component, (typeResolversInformationModelProperty -> getResolverInformation(typeResolversInformationModelProperty)));
    checkArgument(component.getModel() != null, "Cannot generate an Output Cache Key for a 'null' component");
    checkArgument(component.getModel() instanceof HasOutputModel,
                  "Cannot generate an Output Cache Key for a component with no output");
  }

  public OutputMetadataResolutionTypeInformation(ComponentParameterization<?> component) {
    super(component, (typeResolversInformationModelProperty -> getResolverInformation(typeResolversInformationModelProperty)));
    checkArgument(component.getModel() != null, "Cannot generate an Output Cache Key for a 'null' component");
    checkArgument(component.getModel() instanceof HasOutputModel,
                  "Cannot generate an Output Cache Key for a component with no output");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MetadataCacheId getComponentTypeMetadataCacheId() {
    return COMPONENT_TYPE_METADATA_CACHE_ID;
  }

  private static Optional<ResolverInformation> getResolverInformation(TypeResolversInformationModelProperty typeResolversInformationModelProperty) {
    return typeResolversInformationModelProperty.getOutputResolver();
  }
}
