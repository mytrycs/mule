/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.ast;

import static java.util.Optional.empty;
import static com.zernova.runtime.api.functional.Either.left;
import static com.zernova.runtime.api.functional.Either.right;
import static com.zernova.runtime.ast.api.ComponentGenerationInformation.EMPTY_GENERATION_INFO;

import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.ast.api.ComponentGenerationInformation;
import com.zernova.runtime.ast.api.ComponentMetadataAst;
import com.zernova.runtime.ast.api.ComponentParameterAst;
import com.zernova.runtime.ast.api.ParameterResolutionException;

import java.util.Optional;

/**
 * AST component that represent a parameter of a configuration intended to be used when there is not a configuration on the
 * application but there are modules from the XML Sdk that have XML Sdk properties with default values.
 */
public class XmlSdkImplicitConfigParameter implements ComponentParameterAst {

  private final ParameterModel parameterModel;
  private final Object value;


  public XmlSdkImplicitConfigParameter(ParameterModel parameterModel, Object value) {
    this.parameterModel = parameterModel;
    this.value = value;
  }

  @Override
  public ParameterModel getModel() {
    return parameterModel;
  }

  @Override
  public ParameterGroupModel getGroupModel() {
    return null;
  }

  @Override
  public Either<String, Object> getValue() {
    return right(value);
  }

  @Override
  public <T> Either<String, Either<ParameterResolutionException, T>> getValueOrResolutionError() {
    return null;
  }

  @Override
  public String getRawValue() {
    return value != null ? value.toString() : null;
  }

  @Override
  public String getResolvedRawValue() {
    return value != null ? value.toString() : null;
  }

  @Override
  public Optional<ComponentMetadataAst> getMetadata() {
    return empty();
  }

  @Override
  public ComponentGenerationInformation getGenerationInformation() {
    return EMPTY_GENERATION_INFO;
  }

  @Override
  public boolean isDefaultValue() {
    return true;
  }
}
