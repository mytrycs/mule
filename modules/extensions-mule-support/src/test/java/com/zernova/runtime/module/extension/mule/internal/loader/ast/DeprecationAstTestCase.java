/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader.ast;

import static com.zernova.test.allure.AllureConstants.ReuseFeature.REUSE;
import static com.zernova.test.allure.AllureConstants.ReuseFeature.ReuseStory.APPLICATION_EXTENSION_MODEL;
import static com.zernova.test.allure.AllureConstants.ReuseFeature.ReuseStory.OPERATIONS;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import org.junit.Test;

@Feature(REUSE)
@Stories({@Story(APPLICATION_EXTENSION_MODEL), @Story(OPERATIONS)})
@Issue("W-11292607")
public class DeprecationAstTestCase extends AbstractZernovaSdkAstTestCase {

  @Override
  protected String getConfigFile() {
    return "mule-deprecations-config.xml";
  }

  @Test
  public void operationDeprecation() {
    ArtifactAst appAst = getArtifactAst();
    ComponentAst deprecatedOperationAst = getTopLevelComponent(appAst, "deprecatedOperation");
    ComponentAst deprecationAst = getChild(deprecatedOperationAst, "deprecated");
    ComponentModel deprecationComponentModel = deprecationAst.getModel(ComponentModel.class).get();
    assertThat(deprecationComponentModel.getDescription(), is("Defines an operation's deprecation."));
  }

  @Test
  public void parameterDeprecation() {
    ArtifactAst appAst = getArtifactAst();
    ComponentAst operationWithDeprecatedParameter = getTopLevelComponent(appAst, "operationWithDeprecatedParameter");
    ComponentAst parametersAst = getChild(operationWithDeprecatedParameter, "parameters");
    ComponentAst deprecatedParameterAst = getChild(parametersAst, "parameter");
    ComponentAst deprecationAst = getChild(deprecatedParameterAst, "deprecated");
    ComponentModel deprecationComponentModel = deprecationAst.getModel(ComponentModel.class).get();
    assertThat(deprecationComponentModel.getDescription(), is("Defines a parameter's deprecation."));
  }
}
