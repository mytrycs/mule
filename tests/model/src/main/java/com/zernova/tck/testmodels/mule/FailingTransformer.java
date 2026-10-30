/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.testmodels.mule;

import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.runtime.api.i18n.I18nMessageFactory;
import com.zernova.runtime.core.api.transformer.AbstractTransformer;

import java.nio.charset.Charset;

public class FailingTransformer extends AbstractTransformer {

  @Override
  protected Object doTransform(Object src, Charset encoding) throws TransformerException {
    throw new TransformerException(I18nMessageFactory.createStaticMessage("Failure"));
  }
}
