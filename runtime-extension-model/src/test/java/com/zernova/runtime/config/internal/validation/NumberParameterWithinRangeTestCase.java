/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.ast.api.validation.Validation.Level.ERROR;
import static com.zernova.test.allure.AllureConstants.ZernovaDsl.MULE_DSL;
import static com.zernova.test.allure.AllureConstants.ZernovaDsl.DslValidationStory.DSL_VALIDATION_STORY;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeTrue;

import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;
import com.zernova.runtime.config.internal.validation.test.AbstractCoreValidationTestCase;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(MULE_DSL)
@Story(DSL_VALIDATION_STORY)
@RunWith(Parameterized.class)
public class NumberParameterWithinRangeTestCase extends AbstractCoreValidationTestCase {

  @Parameters(name = "ignoreParamsWithProperties: {0}")
  public static List<Boolean> params() {
    return asList(false, true);
  }

  private final boolean ignoreParamsWithProperties;

  public NumberParameterWithinRangeTestCase(boolean ignoreParamsWithProperties) {
    this.ignoreParamsWithProperties = ignoreParamsWithProperties;
  }

  @Override
  protected Validation getValidation() {
    return new NumberParameterWithinRange(ignoreParamsWithProperties);
  }

  @Test
  @Issue("W-15959903")
  public void propertyValue() {
    assumeTrue(ignoreParamsWithProperties);

    final Optional<ValidationResultItem> msg = runValidation("NumberParameterWithinRangeTestCase#propertyValue",
                                                             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                 "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                                                                 "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                                 "    xsi:schemaLocation=\"https://schemas.zernova.dev/core"
                                                                 +
                                                                 "                         https://schemas.zernova.dev/core/current/zernova.xsd\">\n"
                                                                 +
                                                                 "    <flow name=\"w-15959903Flow\" maxConcurrency=\"${maxConcurrency}\">\n"
                                                                 +
                                                                 "        <logger level=\"INFO\" />\n" +
                                                                 "    </flow>\n" +
                                                                 "</zernova>")
                                                                     .stream().findFirst();

    // No errors
    assertThat(msg.isPresent(), is(false));
  }

  @Test
  public void valueOffRange() {
    final Optional<ValidationResultItem> msg = runValidation("NumberParameterWithinRangeTestCase#valueOffRange",
                                                             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                 "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                                                                 "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                                 "    xsi:schemaLocation=\"https://schemas.zernova.dev/core"
                                                                 +
                                                                 "                         https://schemas.zernova.dev/core/current/zernova.xsd\">\n"
                                                                 +
                                                                 "    <flow name=\"w-15959903Flow\" maxConcurrency=\"-1\">\n" +
                                                                 "        <logger level=\"INFO\" />\n" +
                                                                 "    </flow>\n" +
                                                                 "</zernova>")
                                                                     .stream().findFirst();

    assertThat(msg.get().getValidation().getLevel(), is(ERROR));
    assertThat(msg.get().getMessage(),
               containsString("Parameter 'maxConcurrency' in element <flow> value '-1' is not within expected range."));
  }


}
