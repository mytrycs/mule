/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.utils;

import static com.zernova.runtime.ast.api.ArtifactType.APPLICATION;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.emptyArtifact;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.module.artifact.activation.api.ast.ArtifactAstUtils.parseAndBuildAppExtensionModel;

import static java.util.Collections.emptyMap;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.xml.AstXmlParser;
import com.zernova.runtime.config.internal.ArtifactAstConfigurationBuilder;
import com.zernova.runtime.config.internal.model.DefaultComponentBuildingDefinitionRegistryFactory;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.module.artifact.activation.api.ast.AstXmlParserSupplier;

import java.util.Set;

public final class AppParserConfigurationBuilder extends AbstractConfigurationBuilder implements AstXmlParserSupplier {

  private final String[] configFiles;

  public AppParserConfigurationBuilder(String[] configFiles) {
    this.configFiles = configFiles;
  }

  @Override
  protected void doConfigure(ZernovaContext muleContext) throws Exception {
    ArtifactAst artifactAst;
    if (configFiles.length == 0) {
      artifactAst = emptyArtifact();
    } else {
      artifactAst = parseAndBuildAppExtensionModel(muleContext.getConfiguration().getId(),
                                                   configFiles, this, muleContext.getExtensionManager().getExtensions(), false,
                                                   muleContext.getExecutionClassLoader(), muleContext.getConfiguration(), null);
    }
    new ArtifactAstConfigurationBuilder(artifactAst, emptyMap(), APP, false,
                                        new DefaultComponentBuildingDefinitionRegistryFactory()
                                            .create(artifactAst.dependencies(),
                                                    artifactAst::dependenciesDsl))
                                                        .configure(muleContext);
  }

  @Override
  public AstXmlParser getParser(Set<ExtensionModel> extensions, boolean disableValidations) {
    return AstXmlParser.builder()
        .withArtifactType(APPLICATION)
        .withExtensionModels(extensions)
        .build();
  }
}
