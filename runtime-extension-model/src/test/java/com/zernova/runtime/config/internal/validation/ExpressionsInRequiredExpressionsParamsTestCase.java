/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static java.util.Optional.empty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;
import com.zernova.runtime.config.internal.validation.test.AbstractCoreValidationTestCase;

import java.util.Optional;

import org.junit.Test;

public class ExpressionsInRequiredExpressionsParamsTestCase extends AbstractCoreValidationTestCase {

  @Override
  protected Validation getValidation() {
    return new ExpressionsInRequiredExpressionsParams(empty(), false);
  }

  @Test
  public void requiredExpression() {
    final Optional<ValidationResultItem> msg = runValidation("ExpressionsInRequiredExpressionsParamsTestCase#requiredExpression",
                                                             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                 "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                                                                 "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                                                                 +
                                                                 "      xsi:schemaLocation=\"\n" +
                                                                 "       https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd\">\n"
                                                                 +
                                                                 "    <configuration correlationIdGeneratorExpression=\"1, 2, 3\"/>\n"
                                                                 +
                                                                 "\n" +
                                                                 "</zernova>")
                                                                     .stream().findFirst();

    assertThat(msg.isPresent(), is(true));
    assertThat(msg.get().getMessage(),
               containsString("A static value ('1, 2, 3') was given for parameter 'correlationIdGeneratorExpression' but it requires an expression"));
  }

  @Test
  public void requiredExpressionPresent() {
    final Optional<ValidationResultItem> msg =
        runValidation("ExpressionsInRequiredExpressionsParamsTestCase#requiredExpressionPresent",
                      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                          "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                          "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                          "      xsi:schemaLocation=\"\n" +
                          "       https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd\">\n"
                          +
                          "    <flow name=\"flow\">\n" +
                          "        <foreach collection=\"#[1, 2, 3]\">\n" +
                          "            <logger message=\"hello\"/>\n" +
                          "        </foreach>\n" +
                          "    </flow>\n" +
                          "\n" +
                          "</zernova>")
                              .stream().findFirst();

    assertThat(msg.isPresent(), is(false));
  }
}
