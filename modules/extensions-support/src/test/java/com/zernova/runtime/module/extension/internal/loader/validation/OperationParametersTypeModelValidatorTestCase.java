/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.validation;

import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.mockParameters;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.toMetadataType;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.validate;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.visitableMock;

import static java.util.Arrays.asList;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.OperationParametersTypeModelValidator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

@SmallTest
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class OperationParametersTypeModelValidatorTestCase extends AbstractZernovaTestCase {

  public static final String EXTENSION_NAME = "extension";
  public static final String OPERATION_NAME = "operation";
  @Mock
  private ExtensionModel extensionModel;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private OperationModel operationModel;

  @Mock
  private ParameterModel parameterModel;

  private ExtensionModelValidator validator = new OperationParametersTypeModelValidator();

  @BeforeEach
  public void before() {
    when(operationModel.getName()).thenReturn(OPERATION_NAME);
    when(parameterModel.getName()).thenReturn("parameterName");
    when(extensionModel.getOperationModels()).thenReturn(asList(operationModel));
    mockParameters(operationModel, parameterModel);
    visitableMock(operationModel);
  }

  @Test
  void eventType() {
    when(parameterModel.getType()).thenReturn(toMetadataType(CoreEvent.class));
    assertThrows(IllegalModelDefinitionException.class, () -> validate(extensionModel, validator));
  }

  @Test
  void messageType() {
    when(parameterModel.getType()).thenReturn(toMetadataType(Message.class));
    assertThrows(IllegalModelDefinitionException.class, () -> validate(extensionModel, validator));
  }

  @Test
  void validType() {
    when(parameterModel.getType()).thenReturn(toMetadataType(Object.class));
    validate(extensionModel, validator);
  }
}
