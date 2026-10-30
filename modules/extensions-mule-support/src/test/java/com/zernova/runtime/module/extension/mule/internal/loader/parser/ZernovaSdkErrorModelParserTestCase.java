/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader.parser;

import static com.zernova.runtime.config.internal.error.ZernovaCoreErrorTypeRepository.MULE_CORE_ERROR_TYPE_REPOSITORY;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.util.IdentifierParsingUtils;
import com.zernova.runtime.extension.api.loader.parser.ErrorModelParser;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Test;

public class ZernovaSdkErrorModelParserTestCase extends AbstractZernovaTestCase {

  // see com.zernova.runtime.extension.api.error.ZernovaErrors
  // ZERNOVA:SOURCE_RESPONSE_SEND is a child from ZERNOVA:SOURCE_RESPONSE
  // ZERNOVA:SOURCE_RESPONSE is a child from ZERNOVA:SOURCE
  // ZERNOVA:SOURCE is a child from ZERNOVA:ANY
  private static final ErrorType SOURCE_RESPONSE_SEND =
      MULE_CORE_ERROR_TYPE_REPOSITORY.getErrorType(IdentifierParsingUtils.parseErrorType("SOURCE_RESPONSE_SEND", "ZERNOVA")).get();

  @Test
  public void parserFromType() {
    ErrorModelParser sourceResponseSendParser = new ZernovaSdkErrorModelParser(SOURCE_RESPONSE_SEND);
    assertThat(sourceResponseSendParser.getNamespace(), is("ZERNOVA"));
    assertThat(sourceResponseSendParser.getType(), is("SOURCE_RESPONSE_SEND"));
    assertThat(sourceResponseSendParser.getParent().isPresent(), is(true));

    ErrorModelParser sourceResponseParser = sourceResponseSendParser.getParent().get();
    assertThat(sourceResponseParser.getNamespace(), is("ZERNOVA"));
    assertThat(sourceResponseParser.getType(), is("SOURCE_RESPONSE"));
    assertThat(sourceResponseParser.getParent().isPresent(), is(true));

    ErrorModelParser sourceParser = sourceResponseParser.getParent().get();
    assertThat(sourceParser.getNamespace(), is("ZERNOVA"));
    assertThat(sourceParser.getType(), is("SOURCE"));
    assertThat(sourceParser.getParent().isPresent(), is(true));

    ErrorModelParser anyParser = sourceParser.getParent().get();
    assertThat(anyParser.getNamespace(), is("ZERNOVA"));
    assertThat(anyParser.getType(), is("ANY"));
    assertThat(anyParser.getParent().isPresent(), is(false));
  }
}
