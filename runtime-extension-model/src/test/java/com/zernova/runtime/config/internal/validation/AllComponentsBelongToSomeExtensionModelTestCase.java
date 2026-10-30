/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.test.allure.AllureConstants.ZernovaDsl.MULE_DSL;
import static com.zernova.test.allure.AllureConstants.ZernovaDsl.DslValidationStory.DSL_VALIDATION_STORY;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;
import com.zernova.runtime.config.internal.validation.test.AbstractCoreValidationTestCase;

import java.util.Optional;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(MULE_DSL)
@Story(DSL_VALIDATION_STORY)
public class AllComponentsBelongToSomeExtensionModelTestCase extends AbstractCoreValidationTestCase {

  @Override
  protected Validation getValidation() {
    return new AllComponentsBelongToSomeExtensionModel();
  }

  @Test
  public void topLevelComponentThatDoesNotBelongToAnyExtensionModel() {
    final Optional<ValidationResultItem> msg =
        runValidation("AllComponentsBelongToSomeExtensionModelTestCase#topLevelComponentThatDoesNotBelongToAnyExtensionModel",
                      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                          "<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\"\n" +
                          "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                          "      xmlns:some=\"http://www.mulesoft.org/schema/mule/some\"\n" +
                          "      xsi:schemaLocation=\"\n" +
                          "       http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd\">\n"
                          +
                          "       http://www.mulesoft.org/schema/mule/some http://www.mulesoft.org/schema/mule/some/current/mule-some.xsd\">\n"
                          +
                          "\n" +
                          "    <some:thing name=\"theSomething\" />\n" +
                          "\n" +
                          "</mule>")
                              .stream().findFirst();
    assertThat(msg.get().getMessage(), containsString("The component 'some:thing' doesn't belong to any extension model"));
  }
}
