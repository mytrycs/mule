/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.artifact.params;

import static com.zernova.runtime.api.metadata.DataType.STRING;
import static com.zernova.runtime.app.declaration.api.fluent.ParameterSimpleValue.plain;
import static com.zernova.runtime.module.tooling.internal.artifact.params.ParameterExtractor.asDataWeaveExpression;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import org.junit.Before;
import org.junit.Test;

public class ParameterExtractorCorrectDWTestCase extends AbstractZernovaContextTestCase {

  private ExpressionManager expressionManager;

  @Before
  public void setUp() throws Exception {
    expressionManager = muleContext.getExpressionManager();
  }

  @Test
  public void simpleString() {
    generateDeclarationAndEvaluate("This is a simple string");
  }

  @Test
  public void doubleQuotes() {
    generateDeclarationAndEvaluate("This is \"not\" so simple");
  }

  @Test
  public void singleQuotes() {
    generateDeclarationAndEvaluate("This is 'not' so simple");
  }

  @Test
  public void escapedSingleQuotes() {
    generateDeclarationAndEvaluate("This is \\'not\\' so simple");
  }

  @Test
  public void multipleEscapesInSingleQuotes() {
    generateDeclarationAndEvaluate("This is \\\\\'not\\\\\' so simple");
  }

  @Test
  public void escapedDoubleQuotes() {
    generateDeclarationAndEvaluate("This is \\\"not\\\" so simple");
  }

  @Test
  public void multipleEscapesInDoubleQuotes() {
    generateDeclarationAndEvaluate("This is \\\\\"not\\\\\" so simple");
  }

  @Test
  public void multipleDoubleQuotes() {
    generateDeclarationAndEvaluate("\"This\" \"is\" \"not\" \"so\" \"simple\"");
  }

  @Test
  public void multipleSingleQuotes() {
    generateDeclarationAndEvaluate("'This' 'is' 'not' 'so' 'simple'");
  }

  @Test
  public void singleQuoteXML() {
    generateDeclarationAndEvaluate("<?xml version='1.0' encoding='UTF-8' standalone='yes'?> <tag>tagValue</tag>");
  }

  @Test
  public void combinedQuotes() {
    generateDeclarationAndEvaluate("'single' and \"double\" quotes");
  }

  @Test
  public void doubleQuoteXML() {
    generateDeclarationAndEvaluate("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <tag>tagValue</tag>");
  }

  @Test
  public void doubleQuotesJson() {
    generateDeclarationAndEvaluate("{\"property\": \"value\"}");
  }

  private void generateDeclarationAndEvaluate(String parameterInput) {
    TypedValue<?> dwExpression = asDataWeaveExpression(plain(parameterInput));
    final String bindingVariable = "bindingVariable";
    BindingContext context = BindingContext.builder().addBinding(bindingVariable, dwExpression).build();
    TypedValue<?> outputValue = expressionManager.evaluate("#[" + bindingVariable + "]", STRING, context);
    assertThat(outputValue.getValue(), equalTo(parameterInput));
  }

}
