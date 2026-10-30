/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.validation;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.validate;
import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.java.api.JavaTypeLoader;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.module.extension.internal.loader.ParameterGroupDescriptor;
import com.zernova.runtime.module.extension.internal.loader.java.property.CompileTimeModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.ParameterGroupModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.runtime.TypeWrapper;
import com.zernova.runtime.module.extension.internal.loader.java.validation.ParameterGroupModelValidator;
import com.zernova.tck.size.SmallTest;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ParameterGroupModelValidatorTestCase {

  @Rule
  public ExpectedException expectedException = none();

  @Mock
  private ExtensionModel extensionModel;

  @Mock
  private OperationModel operationModel;

  @Mock
  private ParameterGroupModel groupModel;

  @Mock
  private ParameterModel parameterModel;

  private ClassTypeLoader typeLoader = new JavaTypeLoader(Thread.currentThread().getContextClassLoader());

  private ParameterGroupModelValidator validator = new ParameterGroupModelValidator();

  @Before
  public void before() {
    when(extensionModel.getModelProperty(CompileTimeModelProperty.class)).thenReturn(of(mock(CompileTimeModelProperty.class)));
    when(extensionModel.getOperationModels()).thenReturn(asList(operationModel));
    when(operationModel.getParameterGroupModels()).thenReturn(asList(groupModel));
  }

  @Test
  public void invalidModelDueToNonInstantiableParameterGroup() {
    expectedException.expect(IllegalModelDefinitionException.class);
    TypeWrapper type = new TypeWrapper(Serializable.class, typeLoader);
    ParameterGroupDescriptor descriptor =
        new ParameterGroupDescriptor("name", type, null, mock(AnnotatedElement.class), null);

    when(groupModel.getModelProperty(ParameterGroupModelProperty.class))
        .thenReturn(of(new ParameterGroupModelProperty(descriptor)));

    when(groupModel.getParameterModels()).thenReturn(asList(parameterModel));
    validate(extensionModel, validator);
  }

  @Test
  public void invalidModelDueEmptyParameterGroup() {
    expectedException.expect(IllegalModelDefinitionException.class);
    ParameterGroupDescriptor descriptor =
        new ParameterGroupDescriptor("name", new TypeWrapper(EmptyGroupPojo.class, typeLoader),
                                     null, mock(AnnotatedElement.class), null);
    when(groupModel.getModelProperty(ParameterGroupModelProperty.class))
        .thenReturn(of(new ParameterGroupModelProperty(descriptor)));

    validate(extensionModel, validator);
  }

  @Test
  public void skipEmptyGroupValidationInRuntimeMode() {
    when(extensionModel.getModelProperty(CompileTimeModelProperty.class)).thenReturn(empty());
    ParameterGroupDescriptor descriptor =
        new ParameterGroupDescriptor("name", new TypeWrapper(EmptyGroupPojo.class, typeLoader),
                                     null, mock(AnnotatedElement.class), null);
    when(groupModel.getModelProperty(ParameterGroupModelProperty.class))
        .thenReturn(of(new ParameterGroupModelProperty(descriptor)));

    validate(extensionModel, validator);
  }

  @Test
  public void invalidModelNonInstantiableMessage() {
    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException
        .expectMessage("The parameter group of type 'java.io.Serializable' should be non abstract with a default constructor.");
    TypeWrapper type = new TypeWrapper(Serializable.class, typeLoader);
    ParameterGroupDescriptor descriptor =
        new ParameterGroupDescriptor("name", type, null, mock(AnnotatedElement.class), null);

    when(groupModel.getModelProperty(ParameterGroupModelProperty.class))
        .thenReturn(of(new ParameterGroupModelProperty(descriptor)));

    when(groupModel.getParameterModels()).thenReturn(asList(parameterModel));
    validate(extensionModel, validator);
  }

  public static class EmptyGroupPojo {

  }
}
