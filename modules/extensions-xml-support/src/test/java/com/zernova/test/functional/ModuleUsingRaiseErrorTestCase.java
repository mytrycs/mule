/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.functional;

import static com.zernova.functional.junit4.matchers.MessageMatchers.hasPayload;
import static com.zernova.runtime.api.component.ComponentIdentifier.builder;
import static com.zernova.runtime.api.exception.ZernovaException.INFO_LOCATION_KEY;
import static com.zernova.runtime.api.exception.ZernovaException.INFO_SOURCE_XML_KEY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.CONNECTIVITY;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ErrorHandlingStory.RAISE_ERROR;
import static com.zernova.test.marvel.MarvelExtension.MARVEL_EXTENSION;
import static com.zernova.test.marvel.drstrange.DrStrangeErrorTypeDefinition.CUSTOM_ERROR;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.test.marvel.drstrange.CustomErrorException;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(ERROR_HANDLING)
@Story(RAISE_ERROR)
public class ModuleUsingRaiseErrorTestCase extends AbstractCeXmlExtensionZernovaArtifactFunctionalTestCase {

  @Override
  protected String getModulePath() {
    return "modules/module-using-raise-error.xml";
  }

  @Override
  protected String getConfigFile() {
    return "flows/flows-with-module-using-raise-error.xml";
  }

  @Test
  public void muleStaticErrorRaised() throws Exception {
    verifyResultFrom("simple", "Could not connect: A module error occurred.");
  }

  @Test
  public void customStaticErrorRaised() throws Exception {
    verifyResultFrom("complex", "Custom error: A custom error occurred.");
  }

  @Test
  public void customErrorCanBeMapped() throws Exception {
    verifyResultFrom("complexMapping", "Handled");
  }

  @Test
  public void simpleWithNoHandling() throws Exception {
    final String flowName = "simpleWithNoHandlingFlow";
    ZernovaException me = (ZernovaException) flowRunner(flowName).runExpectingException();
    // Needed to be sure the com.zernova.runtime.core.internal.exception.MessagingExceptionLocationProvider.getContextInfo() does
    // decorate the ZernovaException info map.
    stopFlowConstruct(flowName);

    assertThat(me.getMessage(), is("A module error occurred."));
    assertThat(me.getExceptionInfo().getErrorType().toString(), is(CONNECTIVITY.toString()));
    assertThat(me.getInfo().get(INFO_SOURCE_XML_KEY),
               is("<module-using-raise-error:fail-raise-error></module-using-raise-error:fail-raise-error>"));
    assertThat(me.getInfo().get(INFO_LOCATION_KEY),
               is("simpleWithNoHandlingFlow/processors/0 @ ModuleUsingRaiseErrorTestCase#simpleWithNoHandling:flows/flows-with-module-using-raise-error.xml:47"));
  }

  @Test
  public void simpleWithCustomJavaException() throws Exception {
    final String flowName = "simpleWithCustomJavaExceptionFlow";
    ZernovaException me = (ZernovaException) flowRunner(flowName)
        .withVariable("food", null)
        .runExpectingException();
    // Needed to be sure the com.zernova.runtime.core.internal.exception.MessagingExceptionLocationProvider.getContextInfo() does
    // decorate the ZernovaException info map.
    stopFlowConstruct(flowName);

    assertThat(me.getMessage(), is("java.lang.NullPointerException"));
    assertThat(me.getExceptionInfo().getErrorType().toString(), is(builder()
        .namespace(MARVEL_EXTENSION.toUpperCase())
        .name(CUSTOM_ERROR.toString())
        .build().toString()));
    assertThat(me.getInfo().get(INFO_SOURCE_XML_KEY),
               is("<module-using-raise-error:fail-custom-exception content=\"#[vars.food]\"></module-using-raise-error:fail-custom-exception>"));
    assertThat(me.getInfo().get(INFO_LOCATION_KEY),
               is("simpleWithCustomJavaExceptionFlow/processors/0 @ ModuleUsingRaiseErrorTestCase#simpleWithCustomJavaException:flows/flows-with-module-using-raise-error.xml:51"));

    // we ensure the real cause of error is the one thrown by the marvel:read-data operation
    assertThat(me.getCause(), instanceOf(CustomErrorException.class));
  }

  private void verifyResultFrom(String flowName, String expectedPayload) throws Exception {
    assertThat(flowRunner(flowName).run().getMessage(), hasPayload(equalTo(expectedPayload)));
  }

}
