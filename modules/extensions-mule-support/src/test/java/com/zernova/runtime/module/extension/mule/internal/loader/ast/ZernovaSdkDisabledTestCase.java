/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader.ast;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.ENABLE_MULE_SDK_PROPERTY;
import static com.zernova.runtime.ast.api.validation.Validation.Level.ERROR;
import static com.zernova.test.allure.AllureConstants.ReuseFeature.REUSE;
import static com.zernova.test.allure.AllureConstants.ReuseFeature.ReuseStory.APPLICATION_EXTENSION_MODEL;
import static com.zernova.test.allure.AllureConstants.ReuseFeature.ReuseStory.OPERATIONS;

import static java.util.stream.Collectors.toList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThrows;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.Validation.Level;
import com.zernova.runtime.ast.api.validation.ValidationResult;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;
import com.zernova.tck.junit4.rule.SystemProperty;

import org.junit.ClassRule;
import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;

@Feature(REUSE)
@Stories({@Story(APPLICATION_EXTENSION_MODEL), @Story(OPERATIONS)})
@Issue("W-11989569")
public class ZernovaSdkDisabledTestCase extends AbstractZernovaSdkAstTestCase {

  @ClassRule
  public static SystemProperty disableMuleSdk = new SystemProperty(ENABLE_MULE_SDK_PROPERTY, "false");

  @Override
  protected String getConfigFile() {
    return null;
  }

  @Override
  protected boolean validateSchema() {
    return true;
  }

  @Test
  public void whenMuleSdkIsDisabledThenAnOperationCanNotBeDefined() {
    ValidationResult validationResult = parseAstExpectingValidationErrors("validation/app-with-simple-operation.xml");
    assertErrorMessages(validationResult,
                        "The component 'operation:def' doesn't belong to any extension model",
                        "The component 'operation:output' doesn't belong to any extension model",
                        "The component 'operation:payload-type' doesn't belong to any extension model",
                        "The component 'operation:body' doesn't belong to any extension model");
    assertAllValidationsLevel(validationResult, ERROR);
  }

  @Test
  public void whenMuleSdkIsDisabledTheNamespaceThisIsNotResolved() {
    var thrown = assertThrows(ZernovaRuntimeException.class, () -> getArtifactAst("validation/app-using-namespace-this.xml"));
    assertThat(thrown.getMessage(),
               containsString("Can't resolve https://schemas.zernova.dev/this/current/zernova-this.xsd, A dependency or plugin might be missing"));
  }

  private void assertAllValidationsLevel(ValidationResult validationResult, Level expectedValidationLevel) {
    assertThat(validationResult.getItems().stream().map(ValidationResultItem::getValidation).map(Validation::getLevel)
        .collect(toList()),
               everyItem(is(expectedValidationLevel)));
  }

  private void assertErrorMessages(ValidationResult validationResult, String... messages) {
    assertThat(validationResult.getItems().stream().map(ValidationResultItem::getMessage).collect(toList()),
               containsInAnyOrder(messages));
  }
}
