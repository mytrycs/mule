/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.loader.parser;

import com.zernova.api.annotation.NoImplement;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.ExpressionSupport;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.ParameterDslConfiguration;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.display.LayoutModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterRole;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthParameterModelProperty;
import com.zernova.runtime.extension.api.loader.parser.metadata.InputResolverModelParser;

import java.util.List;
import java.util.Optional;

/**
 * Parses the syntactic definition of a {@link ParameterModel} so that the semantics reflected in it can be extracted in a uniform
 * way, regardless of the actual syntax used by the extension developer.
 *
 * @see ExtensionModelParser
 * @since 1.10.0
 */
@NoImplement
public interface ParameterModelParser
    extends SemanticTermsParser, AllowedStereotypesModelParser, AdditionalPropertiesModelParser {

  /**
   * @return the parameter's name
   */
  String getName();

  /**
   * @return the parameter's description
   */
  String getDescription();

  /**
   * @return a {@link MetadataType} describing the parameter's type
   */
  MetadataType getType();

  /**
   * @return whether the parameter is required
   */
  boolean isRequired();

  /**
   * @return the parameter's default value or {@code null} if the parameter is required or no default value provided
   */
  Object getDefaultValue();

  /**
   * @return the parameter's {@link ParameterRole role}
   */
  ParameterRole getRole();

  /**
   * @return the parameter's {@link ExpressionSupport}
   */
  ExpressionSupport getExpressionSupport();

  /**
   * @return the parameter's {@link LayoutModel} if one was defined
   */
  Optional<LayoutModel> getLayoutModel();

  /**
   * @return the parameter's {@link ParameterDslConfiguration} if one was defined
   */
  Optional<ParameterDslConfiguration> getDslConfiguration();

  /**
   * @return whether this parameter should be skipped when generating the extension's connectivity schemas
   */
  boolean isExcludedFromConnectivitySchema();

  /**
   * @return whether this parameter is a config override
   */
  boolean isConfigOverride();

  /**
   * @return whether this parameter acts as the id of the owning component
   */
  boolean isComponentId();

  /**
   * @return the parameter's {@link DeprecationModel} if one was defined
   */
  Optional<DeprecationModel> getDeprecationModel();

  /**
   * @return the parameter's {@link DisplayModel}
   */
  Optional<DisplayModel> getDisplayModel();

  /**
   * @return the parameter's {@link OAuthParameterModelProperty}
   */
  Optional<OAuthParameterModelProperty> getOAuthParameterModelProperty();

  /**
   * @return a {@link ZernovaVersion} representing the minimum mule version this component can run on
   */
  Optional<MinZernovaVersionParser> getResolvedMinZernovaVersion();

  /**
   * @return the parameter's {@link InputResolverModelParser} if dynamic metadata were defined
   */
  Optional<InputResolverModelParser> getInputResolverModelParser();

  /**
   * @return the order and if the part is resolved by a key resolver if metadata key part is defined on the parameter
   */
  Optional<Pair<Integer, Boolean>> getMetadataKeyPart();

}
