/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.dsl.model;

import static java.util.Optional.empty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.serialization.ArtifactAstDeserializer;
import com.zernova.runtime.ast.api.serialization.ArtifactAstSerializerProvider;
import com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;

public class ComponentLocationTestCase extends AbstractDslModelTestCase {

  private static final String CONFIG_NAME = "myConfig";

  private Set<ExtensionModel> extensions;

  @Before
  public void setUp() {
    extensions = ImmutableSet.<ExtensionModel>builder()
        .add(ZernovaExtensionModelProvider.getExtensionModel())
        .add(mockExtension)
        .build();
  }

  @Test
  public void validateConnectionLocation() throws Exception {
    ArtifactAstDeserializer defaultArtifactAstDeserializer = new ArtifactAstSerializerProvider().getDeserializer();

    ArtifactAst applicationModel = defaultArtifactAstDeserializer
        .deserialize(this.getClass().getResourceAsStream("/asts/ComponentLocationTestCase.ast"), name -> extensions.stream()
            .filter(x -> x.getName().equals(name))
            .findFirst()
            .orElse(null));

    ComponentAst config = applicationModel.topLevelComponentsStream().findFirst().get();
    assertThat(config.getModel(ConfigurationModel.class), is(not(empty())));
    assertThat(config.getLocation().getLocation(), equalTo(CONFIG_NAME));

    ComponentAst connection = config.directChildrenStream().findFirst().get();
    assertThat(connection.getModel(ConnectionProviderModel.class), is(not(empty())));
    assertThat(connection.getLocation().getLocation(), equalTo(CONFIG_NAME + "/connection"));
  }

}
