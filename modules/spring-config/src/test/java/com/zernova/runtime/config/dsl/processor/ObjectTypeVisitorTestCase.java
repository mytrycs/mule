/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.dsl.processor;

import static java.lang.Thread.currentThread;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;
import static com.zernova.runtime.ast.api.ComponentMetadataAst.EMPTY_METADATA;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromConfigurationAttribute;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;

import com.zernova.metadata.api.model.StringType;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.ast.api.builder.ComponentAstBuilder;
import com.zernova.runtime.config.internal.dsl.processor.ObjectTypeVisitor;
import com.zernova.runtime.core.internal.processor.LoggerMessageProcessor;
import com.zernova.runtime.dsl.api.component.TypeDefinition;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.Mock;

public class ObjectTypeVisitorTestCase extends AbstractZernovaTestCase {

  private static final String LOGGER_PROCESSOR_FQCN = LoggerMessageProcessor.class.getName();

  @Mock(lenient = true)
  private StringType stringType;

  @BeforeClass
  public static void loadClassLoader() throws ClassNotFoundException {
    currentThread().getContextClassLoader().loadClass(LOGGER_PROCESSOR_FQCN);
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();


  @Test
  public void typeIsInstanceOfGivenClass() {
    ObjectTypeVisitor visitor = new ObjectTypeVisitor(baseComponentModelBuilder().build());
    TypeDefinition typeDefinition = fromType(String.class);
    typeDefinition.visit(visitor);
    assertTrue(String.class.isAssignableFrom(visitor.getType()));

  }

  @Test
  public void typeIsInstanceOfGivenClassFromAttribute() {
    ObjectTypeVisitor visitor = new ObjectTypeVisitor(baseComponentModelBuilder()
        .withRawParameter("type", LOGGER_PROCESSOR_FQCN).build());
    TypeDefinition typeDefinition = fromConfigurationAttribute("type");
    typeDefinition.visit(visitor);
    assertTrue(LoggerMessageProcessor.class.isAssignableFrom(visitor.getType()));
  }

  @Test
  public void typeIsInstanceOfCheckedClassFromAttribute() {
    ObjectTypeVisitor visitor = new ObjectTypeVisitor(baseComponentModelBuilder()
        .withRawParameter("type", LOGGER_PROCESSOR_FQCN).build());
    TypeDefinition typeDefinition = fromConfigurationAttribute("type")
        .checkingThatIsClassOrInheritsFrom(LoggerMessageProcessor.class);
    typeDefinition.visit(visitor);
    assertTrue(LoggerMessageProcessor.class.isAssignableFrom(visitor.getType()));
  }

  @Test
  public void typeIsInstanceOfClassInheritedFromCheckedClassFromAttribute() {
    ObjectTypeVisitor visitor = new ObjectTypeVisitor(baseComponentModelBuilder()
        .withRawParameter("type", LOGGER_PROCESSOR_FQCN).build());
    // Check that ReferenceProcessor inherits from AbstractProcessor
    TypeDefinition typeDefinition = fromConfigurationAttribute("type")
        .checkingThatIsClassOrInheritsFrom(AbstractComponent.class);
    typeDefinition.visit(visitor);
    assertTrue(AbstractComponent.class.isAssignableFrom(visitor.getType()));
  }

  @Test
  public void testFailsIfTypeIsNotOfCheckedClass() {
    exception.expect(ZernovaRuntimeException.class);
    exception.expectMessage("is not the same nor inherits from");
    ObjectTypeVisitor visitor = new ObjectTypeVisitor(baseComponentModelBuilder()
        .withRawParameter("type", this.getClass().getName()).build());
    TypeDefinition typeDefinition = fromConfigurationAttribute("type")
        .checkingThatIsClassOrInheritsFrom(LoggerMessageProcessor.class);
    typeDefinition.visit(visitor);
  }


  private ComponentAstBuilder baseComponentModelBuilder() {
    ParameterModel parameterModel = mock(ParameterModel.class);
    when(parameterModel.getName()).thenReturn("type");
    when(parameterModel.getType()).thenReturn(stringType);

    ParameterGroupModel defaultGroup = mock(ParameterGroupModel.class);
    when(defaultGroup.isShowInDsl()).thenReturn(false);
    when(defaultGroup.getName()).thenReturn(DEFAULT_GROUP_NAME);
    when(defaultGroup.getParameterModels()).thenReturn(singletonList(parameterModel));
    when(defaultGroup.getParameter("flat")).thenReturn(of(parameterModel));

    ParameterizedModel parameterized = mock(ParameterizedModel.class);
    when(parameterized.getAllParameterModels()).thenReturn(singletonList(parameterModel));
    when(parameterized.getParameterGroupModels()).thenReturn(singletonList(defaultGroup));

    return ComponentAstBuilder.builder()
        .withIdentifier(ComponentIdentifier.builder().namespace("ns").name("comp").build())
        .withParameterizedModel(parameterized)
        .withMetadata(EMPTY_METADATA);
  }

}
