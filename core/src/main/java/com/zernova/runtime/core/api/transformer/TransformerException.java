/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.transformer;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.core.internal.transformer.TransformerUtils;

import java.util.List;

/**
 * <code>TransformerException</code> is a simple exception that is thrown by transformers.
 */

@NoExtend
public class TransformerException extends ZernovaException {

  private static final String TRANSFORMER = "Transformer";

  /**
   * Serial version
   */
  private static final long serialVersionUID = 2943589828020763651L;

  private transient Transformer transformer;

  /**
   * @param message the exception message
   */
  public TransformerException(I18nMessage message, Transformer transformer) {
    super(message);
    this.transformer = transformer;
    addInfo(TRANSFORMER, transformer.toString());
  }

  public TransformerException(I18nMessage message, List<Transformer> transformers) {
    super(message);
    this.transformer = TransformerUtils.firstOrNull(transformers);
    addInfo(TRANSFORMER, TransformerUtils.toString(transformers));
  }

  /**
   * @param message the exception message
   * @param cause   the exception that cause this exception to be thrown
   */
  public TransformerException(I18nMessage message, Transformer transformer, Throwable cause) {
    super(message, cause);
    this.transformer = transformer;
    addInfo(TRANSFORMER, transformer.toString());
  }

  public TransformerException(I18nMessage message, List<Transformer> transformers, Throwable cause) {
    super(message, cause);
    this.transformer = TransformerUtils.firstOrNull(transformers);
    addInfo(TRANSFORMER, TransformerUtils.toString(transformers));
  }

  public TransformerException(Transformer transformer, Throwable cause) {
    super(cause);
    this.transformer = transformer;
    addInfo(TRANSFORMER, (transformer == null ? "null" : transformer.toString()));
  }

  public TransformerException(List<Transformer> transformers, Throwable cause) {
    super(cause);
    this.transformer = TransformerUtils.firstOrNull(transformers);
    addInfo(TRANSFORMER, TransformerUtils.toString(transformers));
  }

  /**
   * @param message the exception message
   * @param cause   the exception that cause this exception to be thrown
   */
  public TransformerException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message the exception message
   */
  public TransformerException(I18nMessage message) {
    super(message);
  }

  public Transformer getTransformer() {
    return transformer;
  }
}
