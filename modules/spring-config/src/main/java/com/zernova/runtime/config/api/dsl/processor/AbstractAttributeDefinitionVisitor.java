/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.api.dsl.processor;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.dsl.api.component.AttributeDefinitionVisitor;
import com.zernova.runtime.dsl.api.component.KeyAttributeDefinitionPair;
import com.zernova.runtime.dsl.api.component.TypeConverter;

import java.util.Optional;

/**
 * Abstract {@link AttributeDefinitionVisitor} so clients do not have to implement every method.
 *
 * @since 4.0
 */
@NoExtend
public class AbstractAttributeDefinitionVisitor implements AttributeDefinitionVisitor {

  @Override
  public void onReferenceObject(Class<?> objectType) {
    doOnOperation("onReferenceObject");
  }

  @Override
  public void onReferenceSimpleParameter(String reference) {
    doOnOperation("onReferenceSimpleParameter");
  }

  @Override
  public void onSoftReferenceSimpleParameter(String softReference) {
    doOnOperation("onSoftReferenceSimpleParameter");
  }

  @Override
  public void onReferenceFixedParameter(String reference) {
    doOnOperation("onReferenceFixedParameter");
  }

  @Override
  public void onFixedValue(Object value) {
    doOnOperation("onFixedValue");
  }

  @Override
  public void onConfigurationParameter(String parameterName, Object defaultValue, Optional<TypeConverter> typeConverter) {
    doOnOperation("onConfigurationParameter");
  }

  public void onReferenceConfigurationParameter(String parameterName, Object defaultValue,
                                                Optional<TypeConverter> typeConverter) {
    doOnOperation("onReferenceConfigurationParameter");
  }

  @Override
  public void onUndefinedSimpleParameters() {
    doOnOperation("onUndefinedSimpleParameters");
  }

  @Override
  public void onUndefinedComplexParameters() {
    doOnOperation("onUndefinedComplexParameters");
  }

  @Override
  public void onComplexChildCollection(Class<?> type, Optional<String> wrapperIdentifier) {
    doOnOperation("onComplexChildCollection");
  }

  @Override
  public void onComplexChildMap(Class<?> keyType, Class<?> valueType, String wrapperIdentifier) {
    doOnOperation("onComplexChildMap");
  }

  @Override
  public void onComplexChild(Class<?> type, Optional<String> wrapperIdentifier, Optional<String> childIdentifier) {
    doOnOperation("onComplexChild");
  }

  @Override
  public void onValueFromTextContent() {
    doOnOperation("onReferenceSimpleParameter");
  }

  @Override
  public void onMultipleValues(KeyAttributeDefinitionPair[] definitions) {
    doOnOperation("onReferenceSimpleParameter");
  }

  protected void doOnOperation(String operation) {
    // Do nothing.
  }
}
