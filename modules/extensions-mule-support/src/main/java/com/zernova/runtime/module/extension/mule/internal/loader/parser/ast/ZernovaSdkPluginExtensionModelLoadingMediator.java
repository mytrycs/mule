/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader.parser.ast;

import static com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType.OPERATION_DEF;

import static java.util.Collections.singleton;
import static java.util.Optional.of;

import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.module.artifact.activation.internal.ast.AbstractZernovaSdkExtensionModelLoadingMediator;
import com.zernova.runtime.module.artifact.activation.internal.ast.ZernovaSdkExtensionModelLoadingMediator;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * {@link ZernovaSdkExtensionModelLoadingMediator} that loads an {@link ExtensionModel} from a plugin's {@link ArtifactAst}.
 *
 * @since 4.5.0
 */
public class ZernovaSdkPluginExtensionModelLoadingMediator extends AbstractZernovaSdkExtensionModelLoadingMediator {

  private static final Set<ComponentType> REUSABLE_COMPONENT_TYPES = singleton(OPERATION_DEF);

  private final String version;
  private final ExtensionModelLoader extensionModelLoader;
  private final Consumer<ExtensionModel> onNewExtensionModel;

  /**
   * Creates a new mediator with the given parameters.
   *
   * @param expressionLanguageMetadataService the {@link ExpressionLanguageMetadataService} used to resolve types.
   * @param artifactExtensionLoadingContext   the artifact's {@link ExtensionLoadingContext}.
   * @param version                           the artifact's version in case the full coordinates are not available.
   * @param extensionModelLoader              the loader to use for loading the {@link ExtensionModel}.
   * @param onNewExtensionModel               a consumer to call if the artifact's {@link ExtensionModel} is created as part of
   *                                          the parsing process.
   */
  public ZernovaSdkPluginExtensionModelLoadingMediator(ExpressionLanguageMetadataService expressionLanguageMetadataService,
                                                    ExtensionLoadingContext artifactExtensionLoadingContext,
                                                    String version,
                                                    ExtensionModelLoader extensionModelLoader,
                                                    Consumer<ExtensionModel> onNewExtensionModel) {
    super(artifactExtensionLoadingContext.getArtifactCoordinates(), of(artifactExtensionLoadingContext),
          expressionLanguageMetadataService);
    this.version = version;
    this.extensionModelLoader = extensionModelLoader;
    this.onNewExtensionModel = onNewExtensionModel;
  }

  @Override
  public Optional<ExtensionModel> loadExtensionModel(ArtifactAst ast, ClassLoader classLoader, Set<ExtensionModel> extensions)
      throws ConfigurationException {
    Optional<ExtensionModel> extensionModel = super.loadExtensionModel(ast, classLoader, extensions);
    extensionModel.ifPresent(onNewExtensionModel);
    return extensionModel;
  }

  @Override
  protected String getVersion() throws ConfigurationException {
    return artifactCoordinates.map(ArtifactCoordinates::getVersion).orElse(version);
  }

  @Override
  protected ExtensionModelLoader getLoader() throws ConfigurationException {
    return extensionModelLoader;
  }

  @Override
  protected boolean containsReusableComponents(ArtifactAst ast) {
    return ast.topLevelComponentsStream()
        .anyMatch(component -> REUSABLE_COMPONENT_TYPES.contains(component.getComponentType()));
  }
}
