/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.test.internal.loader.validator;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.meta.model.stereotype.StereotypeModelBuilder.newStereotype;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.PROCESSOR;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.VALIDATOR;
import com.zernova.metadata.api.model.StringType;
import com.zernova.metadata.api.model.VoidType;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.error.ErrorModel;
import com.zernova.runtime.api.meta.model.error.ImmutableErrorModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.extension.api.error.ZernovaErrors;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;
import com.zernova.runtime.extension.internal.loader.validator.ValidatorModelValidator;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class ValidatorModelValidatorTestCase {

  @Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"Mule error and stereotype", false, false}, {"Child error and mule stereotype", true, false},
        {"Child error and Child stereotype", true, true}, {"Mule error and child stereotype", false, true},
    });
  }

  private final boolean childError;
  private final boolean childStereotype;

  public ValidatorModelValidatorTestCase(String name, boolean childError, boolean childStereotype) {
    this.childError = childError;
    this.childStereotype = childStereotype;
  }

  @Mock
  private ExtensionModel extensionModel;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private OperationModel operationModel;

  private ValidatorModelValidator validator = new ValidatorModelValidator();
  private ProblemsReporter reporter = new ProblemsReporter(extensionModel);

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    StereotypeModel validationStereotype = newStereotype(VALIDATOR.getType(), VALIDATOR.getNamespace())
        .withParent(newStereotype(PROCESSOR.getType(), PROCESSOR.getNamespace()).build())
        .build();

    if (childStereotype) {
      validationStereotype = newStereotype("customValidator", "customExtension")
          .withParent(validationStereotype)
          .build();
    }

    when(operationModel.getStereotype()).thenReturn(validationStereotype);

    ErrorModel errorModel = new ImmutableErrorModel(ZernovaErrors.VALIDATION.name(), "ZERNOVA", true, null);
    if (childError) {
      errorModel = new ImmutableErrorModel("customValidation", "customExtension", true, errorModel);
    }

    when(operationModel.getErrorModels()).thenReturn(singleton(errorModel));
    when(operationModel.getOutput().getType()).thenReturn(mock(VoidType.class));
    when(extensionModel.getOperationModels()).thenReturn(singletonList(operationModel));
  }

  @Test
  public void valid() {
    validator.validate(extensionModel, reporter);
    assertThat(reporter.hasErrors(), is(false));
  }

  private void assertFail() {
    validator.validate(extensionModel, reporter);
    assertThat(reporter.hasErrors(), is(true));
  }

  @Test
  public void nonVoidType() {
    when(operationModel.getOutput().getType()).thenReturn(mock(StringType.class));
    assertFail();
  }

  @Test
  public void nonValidationOrphanError() {
    ErrorModel errorModel = new ImmutableErrorModel("custom", "custom", true, null);
    when(operationModel.getErrorModels()).thenReturn(singleton(errorModel));

    assertFail();
  }

  @Test
  public void nonValidationChildError() {
    ErrorModel errorModel = new ImmutableErrorModel("custom", "custom", true, null);
    errorModel = new ImmutableErrorModel("customChild", "custom", true, errorModel);
    when(operationModel.getErrorModels()).thenReturn(singleton(errorModel));

    assertFail();
  }
}
