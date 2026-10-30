/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.ast.api.validation.Validation.Level.WARN;
import static com.zernova.test.allure.AllureConstants.ZernovaDsl.MULE_DSL;
import static com.zernova.test.allure.AllureConstants.ZernovaDsl.DslValidationStory.DSL_VALIDATION_STORY;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;
import com.zernova.runtime.config.internal.validation.test.AbstractCoreValidationTestCase;

import java.util.Optional;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(MULE_DSL)
@Story(DSL_VALIDATION_STORY)
public class RoundRobinRoutesTestCase extends AbstractCoreValidationTestCase {

  @Override
  protected Validation getValidation() {
    return new RoundRobinRoutes();
  }

  @Test
  public void roundRobinRoutes() {
    final Optional<ValidationResultItem> msg = runValidation("RoundRobinRoutesTestCase#roundRobinRoutes",
                                                             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                 "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                                                                 "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                                                                 +
                                                                 "      xsi:schemaLocation=\"https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd\">\n"
                                                                 +
                                                                 "\n" +
                                                                 "    <flow name=\"flow\">\n" +
                                                                 "        <round-robin>\n" +
                                                                 "            <route>\n" +
                                                                 "                <logger/>\n" +
                                                                 "            </route>\n" +
                                                                 "        </round-robin>\n" +
                                                                 "    </flow>\n" +
                                                                 "    \n" +
                                                                 "</zernova>")
                                                                     .stream().findFirst();

    assertThat(msg.get().getValidation().getLevel(), is(WARN));
    assertThat(msg.get().getMessage(),
               containsString("At least 2 routes are required for 'round-robin'."));
  }
}
