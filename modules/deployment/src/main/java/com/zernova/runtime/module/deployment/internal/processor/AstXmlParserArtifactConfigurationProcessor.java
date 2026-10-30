/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.internal.processor;

import static com.zernova.runtime.api.config.FeatureFlaggingService.FEATURE_FLAGGING_SERVICE_KEY;
import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.ENTITY_RESOLVER_FAIL_ON_FIRST_ERROR;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.emptyArtifact;
import static com.zernova.runtime.config.api.dsl.ArtifactDeclarationUtils.toArtifactast;
import static com.zernova.runtime.config.internal.ApplicationFilteredFromPolicyArtifactAst.applicationFilteredFromPolicyArtifactAst;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.POLICY;
import static com.zernova.runtime.module.artifact.activation.api.ast.ArtifactAstUtils.parseAndBuildAppExtensionModel;

import static java.util.Collections.emptySet;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.app.declaration.api.ArtifactDeclaration;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.xml.AstXmlParser;
import com.zernova.runtime.ast.api.xml.AstXmlParser.Builder;
import com.zernova.runtime.config.api.properties.ConfigurationPropertiesHierarchyBuilder;
import com.zernova.runtime.config.api.properties.ConfigurationPropertiesResolver;
import com.zernova.runtime.config.internal.ArtifactAstConfigurationBuilder;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.registry.ZernovaRegistryHelper;
import com.zernova.runtime.core.internal.registry.Registry;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContextConfiguration;

import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link ArtifactConfigurationProcessor} that parses the XML configuration files and delegates to
 * {@link ArtifactAstConfigurationBuilder} to create registry and populate the {@link ZernovaContext}.
 *
 * @since 4.5
 */
public final class AstXmlParserArtifactConfigurationProcessor extends AbstractAstConfigurationProcessor {

  @Override
  protected ArtifactAst obtainArtifactAst(ArtifactContextConfiguration artifactContextConfiguration)
      throws ConfigurationException {
    return createApplicationModel(artifactContextConfiguration.getMuleContext(),
                                  artifactContextConfiguration.getArtifactDeclaration(),
                                  artifactContextConfiguration.getConfigResources(),
                                  artifactContextConfiguration.getArtifactProperties(),
                                  artifactContextConfiguration.getArtifactType(),
                                  artifactContextConfiguration.getParentArtifactContext()
                                      .map(ArtifactContext::getArtifactAst)
                                      .orElse(emptyArtifact()),
                                  artifactContextConfiguration.isDisableXmlValidations(),
                                  artifactContextConfiguration.getExpressionLanguageMetadataService());
  }

  private Set<ExtensionModel> getExtensions(ExtensionManager extensionManager) {
    return extensionManager == null ? emptySet() : extensionManager.getExtensions();
  }

  private ArtifactAst createApplicationModel(ZernovaContext muleContext,
                                             ArtifactDeclaration artifactDeclaration,
                                             String[] artifactConfigResources,
                                             Map<String, String> artifactProperties,
                                             ArtifactType artifactType,
                                             ArtifactAst parentArtifactAst,
                                             boolean disableXmlValidations,
                                             ExpressionLanguageMetadataService expressionLanguageMetadataService)
      throws ConfigurationException {

    Set<ExtensionModel> extensions = getExtensions(muleContext.getExtensionManager());

    try {
      final ArtifactAst artifactAst;

      if (artifactDeclaration == null) {
        if (artifactConfigResources.length == 0) {
          artifactAst = emptyArtifact();
        } else {
          artifactAst = parseAndBuildAppExtensionModel(muleContext.getConfiguration().getId(),
                                                       artifactConfigResources,
                                                       (exts, disableValidations) -> createMuleXmlParser(muleContext, exts,
                                                                                                         artifactProperties,
                                                                                                         artifactType,
                                                                                                         parentArtifactAst,
                                                                                                         disableValidations),
                                                       extensions,
                                                       disableXmlValidations,
                                                       muleContext.getExecutionClassLoader(),
                                                       muleContext.getConfiguration(),
                                                       expressionLanguageMetadataService);
        }
      } else {
        artifactAst = toArtifactast(artifactDeclaration, extensions);
      }

      return artifactAst;
    } catch (ConfigurationException e) {
      throw e;
    } catch (Exception e) {
      throw new ConfigurationException(e);
    }
  }

  private AstXmlParser createMuleXmlParser(ZernovaContext muleContext,
                                           Set<ExtensionModel> extensions,
                                           Map<String, String> artifactProperties,
                                           ArtifactType artifactType,
                                           ArtifactAst parentArtifactAst,
                                           boolean disableXmlValidations) {
    ConfigurationPropertiesResolver propertyResolver = new ConfigurationPropertiesHierarchyBuilder()
        .withApplicationProperties(artifactProperties)
        .build();

    FeatureFlaggingService featureFlaggingService = getFeatureFlaggingService(muleContext);
    Builder builder = AstXmlParser.builder()
        .withPropertyResolver(propertyKey -> (String) propertyResolver.resolveValue(propertyKey))
        .withExtensionModels(extensions)
        .withArtifactType(toAstArtifactType(artifactType))
        .withParentArtifact(resolveParentArtifact(parentArtifactAst, artifactType, featureFlaggingService));
    if (!featureFlaggingService.isEnabled(ENTITY_RESOLVER_FAIL_ON_FIRST_ERROR)) {
      builder.withLegacyFailStrategy();
    }
    if (disableXmlValidations) {
      builder.withSchemaValidationsDisabled();
    }

    return builder.build();
  }

  private com.zernova.runtime.ast.api.ArtifactType toAstArtifactType(ArtifactType artifactType) {
    switch (artifactType) {
      case APP:
        return com.zernova.runtime.ast.api.ArtifactType.APPLICATION;
      case DOMAIN:
        return com.zernova.runtime.ast.api.ArtifactType.DOMAIN;
      case POLICY:
        return com.zernova.runtime.ast.api.ArtifactType.POLICY;
      default:
        throw new IllegalArgumentException("The provided artifact type '" + artifactType + "' cannot be deployed.");
    }
  }

  private FeatureFlaggingService getFeatureFlaggingService(ZernovaContext muleContext) {
    Registry originalRegistry = ((ZernovaRegistryHelper) (((ZernovaContextWithRegistry) muleContext).getRegistry())).getDelegate();
    return originalRegistry.lookupObject(FEATURE_FLAGGING_SERVICE_KEY);
  }

  private ArtifactAst resolveParentArtifact(ArtifactAst parentArtifactAst, ArtifactType artifactType,
                                            FeatureFlaggingService featureFlaggingService) {
    if (POLICY.equals(artifactType)) {
      return applicationFilteredFromPolicyArtifactAst(parentArtifactAst, featureFlaggingService);
    }
    return parentArtifactAst;
  }

}
