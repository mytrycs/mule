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

import static java.lang.String.format;

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
public class SingletonsAreNotRepeatedTestCase extends AbstractCoreValidationTestCase {

  private static final String REPEATED_ELEMENT_NAME = "configuration";

  @Override
  protected Validation getValidation() {
    return new SingletonsAreNotRepeated();
  }

  @Test
  public void conflictingNames() {
    final Optional<ValidationResultItem> msg = runValidation("SingletonsAreNotRepeatedTestCase#conflictingNames",
                                                             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                 "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                                                                 "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                                                                 +
                                                                 "      xsi:schemaLocation=\"https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd\">\n"
                                                                 +
                                                                 "\n" +
                                                                 "    <" + REPEATED_ELEMENT_NAME
                                                                 + " defaultResponseTimeout=\"10000\"/>\n" +
                                                                 "\n" +
                                                                 "    <flow name=\"firstFlow\">\n" +
                                                                 "        <logger/>\n" +
                                                                 "    </flow>\n" +
                                                                 "    \n" +
                                                                 "</zernova>",
                                                             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                 "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                                                                 "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                                                                 +
                                                                 "      xsi:schemaLocation=\"https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd\">\n"
                                                                 +
                                                                 "\n" +
                                                                 "    <flow name=\"secondFlow\">\n" +
                                                                 "        <logger/>\n" +
                                                                 "    </flow>\n" +
                                                                 "\n" +
                                                                 "    <" + REPEATED_ELEMENT_NAME
                                                                 + " defaultTransactionTimeout=\"30000\"/>\n" +
                                                                 "\n" +
                                                                 "</zernova>\n" +
                                                                 "")
                                                                     .stream().findFirst();

    assertThat(msg.get().getMessage(),
               containsString(format("The configuration element '%s' can only appear once",
                                     REPEATED_ELEMENT_NAME)));
  }

}
