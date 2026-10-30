/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;
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
import io.qameta.allure.Features;
import io.qameta.allure.Story;

@Features({@Feature(ERROR_HANDLING), @Feature(MULE_DSL)})
@Story(DSL_VALIDATION_STORY)
public class ErrorHandlerOnErrorHasTypeOrWhenTestCase extends AbstractCoreValidationTestCase {

  @Override
  protected Validation getValidation() {
    return new ErrorHandlerOnErrorHasTypeOrWhen();
  }

  @Test
  public void errorHandlerCantHaveOnErrorWithoutTypeOrExpression() {
    final Optional<ValidationResultItem> msg =
        runValidation("ErrorHandlerOnErrorHasTypeOrWhenTestCase#errorHandlerCantHaveOnErrorWithoutTypeOrExpression",
                      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                          "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                          "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                          "      xsi:schemaLocation=\"\n" +
                          "       https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd\">\n"
                          +
                          "\n" +
                          "    <flow name=\"flowA\">\n" +
                          "        <logger/>\n" +
                          "        <error-handler>\n" +
                          "            <on-error-continue/>\n" +
                          "            <on-error-continue/>\n" +
                          "        </error-handler>\n" +
                          "    </flow>\n" +
                          "\n" +
                          "</zernova>")
                              .stream().findFirst();

    assertThat(msg.get().getMessage(),
               containsString("Every handler (except for the last one) within an 'error-handler' must specify a 'when' or 'type' attribute."));
  }

  @Test
  public void onErrorReferenceMustHaveTypeOrExpression() {
    final Optional<ValidationResultItem> msg =
        runValidation("ErrorHandlerOnErrorHasTypeOrWhenTestCase#onErrorReferenceMustHaveTypeOrExpression",
                      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                          "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                          "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                          "      xsi:schemaLocation=\"\n" +
                          "       https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd\">\n"
                          +
                          "\n" +
                          "    <on-error-propagate name=\"sharedOnError\">\n" +
                          "        <logger/>\n" +
                          "    </on-error-propagate>\n" +
                          "\n" +
                          "    <flow name=\"flowA\">\n" +
                          "        <logger/>\n" +
                          "        <error-handler>\n" +
                          "            <on-error ref=\"sharedOnError\"/>\n" +
                          "            <on-error-continue/>\n" +
                          "        </error-handler>\n" +
                          "    </flow>\n" +
                          "\n" +
                          "</zernova>")
                              .stream().findFirst();

    assertThat(msg.get().getMessage(),
               containsString("Every handler (except for the last one) within an 'error-handler' must specify a 'when' or 'type' attribute."));
  }

}
