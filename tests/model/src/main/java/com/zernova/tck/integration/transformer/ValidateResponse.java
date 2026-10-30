/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.integration.transformer;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.runtime.core.api.util.IOUtils;

import java.io.InputStream;

/**
 * Throws an exception if the message does not contain "success".
 */
public class ValidateResponse extends AbstractComponent implements Processor {

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {

    final Object src = event.getMessage().getPayload().getValue();
    String response = null;
    if (src instanceof String) {
      response = (String) src;
    } else if (src instanceof InputStream) {
      response = IOUtils.toString((InputStream) src);
    }

    if (response != null && response.contains("success")) {
      return event;
    } else {
      throw new TransformerException(createStaticMessage("Invalid response from flow: " + response));
    }
  }

}


