/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.el.function;

import static com.zernova.runtime.api.metadata.DataType.STRING;

import static java.util.Collections.singletonList;
import static java.util.Optional.of;

import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.el.ExpressionFunction;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.FunctionParameter;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of p(), a function which returns the value of a property, if any, first checking the configuration ones, then
 * the system ones and finally the environment ones.
 *
 * @since 4.0
 */
public class PropertyAccessFunction implements ExpressionFunction {

  private volatile ConfigurationProperties configurationProperties;

  @Override
  public Object call(Object[] parameters, BindingContext context) {
    String name = (String) parameters[0];
    return configurationProperties.resolveStringProperty(name).orElse(null);
  }

  @Override
  public Optional<DataType> returnType() {
    return of(STRING);
  }

  @Override
  public List<FunctionParameter> parameters() {
    return singletonList(new FunctionParameter("name", STRING));
  }

  public void setConfigurationProperties(ConfigurationProperties configurationProperties) {
    this.configurationProperties = configurationProperties;
  }
}
