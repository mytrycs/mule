/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.ast;

import static com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType.OPERATION_DEF;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.extension.api.ExtensionConstants.MULE_SDK_APPLICATION_LOADER_ID;
import static com.zernova.runtime.extension.api.ExtensionConstants.MULE_SDK_EXTENSION_NAME_PROPERTY_NAME;
import static com.zernova.runtime.module.artifact.activation.api.extension.discovery.boot.ExtensionLoaderUtils.getOptionalLoaderById;

import static java.util.Collections.singleton;

import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest;

import java.util.Optional;
import java.util.Set;

/**
 * An {@link ZernovaSdkExtensionModelLoadingMediator} suitable for the context of applications.
 *
 * @since 4.5.0
 */
public class ZernovaSdkApplicationExtensionModelLoadingMediator extends AbstractZernovaSdkExtensionModelLoadingMediator {

  // TODO W-11796759: This class shouldn't know which are the specific reusable components.
  private static final Set<ComponentType> REUSABLE_COMPONENT_TYPES = singleton(OPERATION_DEF);

  private final String artifactId;

  public ZernovaSdkApplicationExtensionModelLoadingMediator(ExpressionLanguageMetadataService expressionLanguageMetadataService,
                                                         String artifactId,
                                                         Optional<ArtifactCoordinates> artifactCoordinates,
                                                         Optional<ExtensionLoadingContext> artifactExtensionLoadingContext) {
    super(artifactCoordinates, artifactExtensionLoadingContext, expressionLanguageMetadataService);
    this.artifactId = artifactId;
  }

  @Override
  protected String getVersion() throws ConfigurationException {
    return artifactCoordinates.map(ArtifactCoordinates::getVersion)
        .orElseThrow(() -> new ConfigurationException(buildErrorMessage("No version specified", artifactId)));
  }

  @Override
  protected ExtensionModelLoader getLoader() throws ConfigurationException {
    return getOptionalLoaderById(MULE_SDK_APPLICATION_LOADER_ID)
        .orElseThrow(() -> new ConfigurationException(buildErrorMessage("Mule ExtensionModelLoader not found", artifactId)));
  }

  @Override
  protected boolean containsReusableComponents(ArtifactAst ast) {
    return ast.topLevelComponentsStream()
        .anyMatch(component -> REUSABLE_COMPONENT_TYPES.contains(component.getComponentType()));
  }

  @Override
  protected void addCustomLoadingRequestParameters(ExtensionModelLoadingRequest.Builder loadingRequestBuilder) {
    super.addCustomLoadingRequestParameters(loadingRequestBuilder);
    loadingRequestBuilder.addParameter(MULE_SDK_EXTENSION_NAME_PROPERTY_NAME, artifactId);
  }

  private I18nMessage buildErrorMessage(String reason, String artifactId) {
    return createStaticMessage("ExtensionModel for application %s not generated: %s", artifactId, reason);
  }
}
