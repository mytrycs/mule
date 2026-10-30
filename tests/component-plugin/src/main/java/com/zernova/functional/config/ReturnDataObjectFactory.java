/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.config;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.util.IOUtils.getResourceAsString;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;

import java.io.IOException;

/**
 * {@link AbstractComponentFactory} for test:component return-data element.
 *
 * @since 4.0
 */
public class ReturnDataObjectFactory extends AbstractComponentFactory<String> {

  private String file;
  private String content;

  public void setFile(String file) {
    this.file = file;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String getObject() throws Exception {
    String returnData = content;
    if (file != null) {
      try {
        returnData = getResourceAsString(file, getClass());
      } catch (IOException e) {
        throw new ZernovaRuntimeException(createStaticMessage("Failed to load test-data resource: " + file), e);
      }
    }
    return returnData;
  }

  @Override
  public String doGetObject() throws Exception {
    throw new UnsupportedOperationException("This factory returns a simple Java String. We can't have annotations on a String");
  }

}
