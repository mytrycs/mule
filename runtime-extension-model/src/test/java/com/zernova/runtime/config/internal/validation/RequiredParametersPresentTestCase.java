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
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;

import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;
import com.zernova.runtime.config.internal.validation.test.AbstractCoreValidationTestCase;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(MULE_DSL)
@Story(DSL_VALIDATION_STORY)
public class RequiredParametersPresentTestCase extends AbstractCoreValidationTestCase {

  @Override
  protected Validation getValidation() {
    return new RequiredParametersPresent();
  }

  @Test
  public void flowRefWithNoName() {
    final Optional<ValidationResultItem> msg = runValidation("RequiredParametersPresentTestCase#flowRefWithNoName",
                                                             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                 "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                                                                 "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                                                                 +
                                                                 "      xsi:schemaLocation=\"https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd\">\n"
                                                                 +
                                                                 "\n" +
                                                                 "    <flow name=\"flow\">\n" +
                                                                 "        <flow-ref/>\n" +
                                                                 "    </flow>\n" +
                                                                 "    \n" +
                                                                 "</zernova>")
                                                                     .stream().findFirst();

    assertThat(msg.get().getMessage(),
               containsString(format("Element <flow-ref> is missing required parameter 'name'.")));
  }

  @Test
  @Issue("MULE-19899")
  public void numberParamWithUnresolvedProperty() {
    final Optional<ValidationResultItem> msg =
        runValidation("RequiredParametersPresentTestCase#numberParamWithUnresolvedProperty",
                      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                          "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                          "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                          "      xmlns:test=\"https://schemas.zernova.dev/test\"\n" +
                          "      xsi:schemaLocation=\"https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd\n"
                          +
                          "               https://schemas.zernova.dev/test https://schemas.zernova.dev/test/current/zernova-test.xsd\">\n"
                          +
                          "\n" +
                          "    <test:other-config name=\"cfg\" count=\"${other.count}\"/>\n" +
                          "\n" +
                          "</zernova>")
                              .stream().findFirst();

    assertThat(msg.map(r -> r.getMessage()).orElse(null), msg.isPresent(), is(false));
  }

  @Test
  @Issue("MULE-19963")
  public void manyRequiredMissingAllReported() {
    final List<ValidationResultItem> msgs = runValidation("RequiredParametersPresentTestCase#manyRequiredMissingAllReported",
                                                          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                              "<zernova xmlns=\"https://schemas.zernova.dev/core\"\n" +
                                                              "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                                              "      xsi:schemaLocation=\"https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd\">\n"
                                                              +
                                                              "\n" +
                                                              "    <global-property/>\n" +
                                                              "\n" +
                                                              "</zernova>");

    assertThat(msgs, hasSize(2));
  }
}
