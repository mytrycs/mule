/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.exception;

import static java.lang.String.format;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.NameUtils.hyphenize;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;

/**
 * {@link ZernovaRuntimeException} that aims to be thrown when a required parameter is not set.
 *
 * @since 4.0
 */
public class RequiredParameterNotSetException extends ZernovaRuntimeException {

  private final String paramName;

  public RequiredParameterNotSetException(ParameterModel parameterModel) {
    this(parameterModel.getName());
  }

  public RequiredParameterNotSetException(String name) {
    super(createStaticMessage(format("Parameter '%s' is required but was not found", hyphenize(name))));
    this.paramName = name;
  }

  public String getParameterName() {
    return paramName;
  }
}
