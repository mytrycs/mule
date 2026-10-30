/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.dsl.model;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static com.zernova.runtime.app.declaration.api.fluent.ElementDeclarer.newListValue;
import static com.zernova.runtime.app.declaration.api.fluent.ElementDeclarer.newParameterGroup;
import static com.zernova.runtime.config.api.dsl.ArtifactDeclarationUtils.toArtifactast;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.app.declaration.api.ArtifactDeclaration;
import com.zernova.runtime.app.declaration.api.fluent.ElementDeclarer;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class ComponentLocationTestCase extends AbstractDslModelTestCase {

  private static final String CONFIG_NAME = "myConfig";

  private ElementDeclarer declarer;
  private Set<ExtensionModel> extensions;

  @Before
  public void setUp() {
    declarer = ElementDeclarer.forExtension(EXTENSION_NAME);
    extensions = ImmutableSet.<ExtensionModel>builder()
        .add(ZernovaExtensionModelProvider.getExtensionModel())
        .add(mockExtension)
        .build();
  }

  @Test
  public void validateConnectionLocation() throws Exception {
    ArtifactAst applicationModel = toArtifactast(buildAppDeclaration(), extensions);

    ComponentAst config = applicationModel.topLevelComponentsStream().findFirst().get();
    assertThat(config.getModel(ConfigurationModel.class), is(not(empty())));
    assertThat(config.getLocation().getLocation(), equalTo(CONFIG_NAME));

    ComponentAst connection = config.directChildrenStream().findFirst().get();
    assertThat(connection.getModel(ConnectionProviderModel.class), is(not(empty())));
    assertThat(connection.getLocation().getLocation(), equalTo(CONFIG_NAME + "/connection"));
  }

  private ArtifactDeclaration buildAppDeclaration() {
    return ElementDeclarer.newArtifact()
        .withGlobalElement(declarer.newConfiguration(CONFIGURATION_NAME)
            .withRefName(CONFIG_NAME)
            .withParameterGroup(newParameterGroup()
                .withParameter(CONTENT_NAME, CONTENT_VALUE)
                .withParameter(BEHAVIOUR_NAME, BEHAVIOUR_VALUE)
                .withParameter(LIST_NAME, newListValue().withValue(ITEM_VALUE).build())
                .getDeclaration())
            .withConnection(declarer.newConnection(CONNECTION_PROVIDER_NAME)
                .withParameterGroup(newParameterGroup()
                    .withParameter(CONTENT_NAME, CONTENT_VALUE)
                    .withParameter(BEHAVIOUR_NAME, BEHAVIOUR_VALUE)
                    .withParameter(LIST_NAME,
                                   newListValue().withValue(ITEM_VALUE).build())
                    .getDeclaration())
                .getDeclaration())
            .getDeclaration())
        .getDeclaration();
  }

}
