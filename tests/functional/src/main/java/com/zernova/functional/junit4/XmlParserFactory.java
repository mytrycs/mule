/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.junit4;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ArtifactType;
import com.zernova.runtime.ast.api.xml.AstXmlParser;
import com.zernova.runtime.ast.api.xml.AstXmlParser.Builder;
import com.zernova.runtime.config.api.properties.ConfigurationPropertiesHierarchyBuilder;
import com.zernova.runtime.config.api.properties.ConfigurationPropertiesResolver;
import com.zernova.runtime.dsl.api.xml.parser.ParsingPropertyResolver;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class XmlParserFactory {

  private final boolean disableXmlValidations;
  private final Set<ExtensionModel> extensions;
  private final Map<String, String> artifactProperties;
  private final ArtifactType artifactType;
  private final ArtifactAst parentArtifactAst;

  public XmlParserFactory(boolean disableXmlValidations,
                          Set<ExtensionModel> extensions,
                          Map<String, String> artifactProperties,
                          ArtifactType artifactType,
                          ArtifactAst parentArtifactAst) {
    this.disableXmlValidations = disableXmlValidations;
    this.extensions = extensions;
    this.artifactProperties = artifactProperties;
    this.artifactType = artifactType;
    this.parentArtifactAst = parentArtifactAst;
  }

  public AstXmlParser createMuleXmlParser() {
    Builder builder = AstXmlParser.builder()
        .withPropertyResolver(createConfigurationPropertiesResolver(artifactProperties))
        .withExtensionModels(extensions)
        .withArtifactType(artifactType)
        .withParentArtifact(parentArtifactAst);
    if (disableXmlValidations) {
      builder.withSchemaValidationsDisabled();
    }

    return builder.build();
  }

  private ParsingPropertyResolver createConfigurationPropertiesResolver(Map<String, String> artifactProperties) {
    ConfigurationPropertiesResolver resolver = new ConfigurationPropertiesHierarchyBuilder()
        .withApplicationProperties(artifactProperties)
        .build();

    return propertyKey -> (String) resolver.resolveValue(propertyKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactProperties, disableXmlValidations, extensions, artifactType, parentArtifactAst);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    XmlParserFactory other = (XmlParserFactory) obj;
    return Objects.equals(artifactProperties, other.artifactProperties)
        && disableXmlValidations == other.disableXmlValidations
        && Objects.equals(extensions, other.extensions)
        && Objects.equals(artifactType, other.artifactType)
        && Objects.equals(parentArtifactAst, other.parentArtifactAst);
  }

}
