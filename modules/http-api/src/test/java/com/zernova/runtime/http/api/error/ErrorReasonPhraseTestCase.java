/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.error;

import static com.zernova.runtime.http.api.HttpConstants.HttpStatus.getReasonPhraseForStatusCode;
import static com.zernova.runtime.http.api.AllureConstants.HttpFeature.HTTP_SERVICE;
import static com.zernova.runtime.http.api.AllureConstants.HttpFeature.HttpStory.ERRORS;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Test;

@Feature(HTTP_SERVICE)
@Story(ERRORS)
public class ErrorReasonPhraseTestCase {

  @Test
  @Issue("W-12293483")
  public void unprocessableEntity422() {
    assertThat(getReasonPhraseForStatusCode(422), is("Unprocessable Entity"));
  }
}
