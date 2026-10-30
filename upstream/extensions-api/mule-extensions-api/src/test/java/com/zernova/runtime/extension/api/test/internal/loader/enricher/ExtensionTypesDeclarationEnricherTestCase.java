/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.test.internal.loader.enricher;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.metadata.api.annotation.TypeAnnotation;
import com.zernova.metadata.api.annotation.TypeIdAnnotation;
import com.zernova.metadata.api.builder.ObjectFieldTypeBuilder;
import com.zernova.metadata.api.builder.ObjectTypeBuilder;
import com.zernova.metadata.api.model.MetadataFormat;
import com.zernova.metadata.api.model.ObjectFieldType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.api.model.impl.DefaultObjectType;
import com.zernova.metadata.java.api.annotation.ClassInformationAnnotation;
import com.zernova.runtime.api.meta.model.SubTypesModel;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.OutputDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclaration;
import com.zernova.runtime.extension.api.declaration.type.annotation.ParameterDslAnnotation;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.internal.loader.enricher.ExtensionTypesDeclarationEnricher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;

public class ExtensionTypesDeclarationEnricherTestCase {

  @Test
  public void subTypesAndTypesAreSameInstance() {
    final ExtensionLoadingContext extensionLoadingContext = mock(ExtensionLoadingContext.class);
    final ExtensionDeclarer extensionDeclarer = mock(ExtensionDeclarer.class);
    final ExtensionDeclaration extensionDeclaration = mock(ExtensionDeclaration.class);
    when(extensionDeclarer.getDeclaration()).thenReturn(extensionDeclaration);
    when(extensionLoadingContext.getExtensionDeclarer()).thenReturn(extensionDeclarer);

    final ParameterGroupDeclaration parameterGroupDeclaration = mock(ParameterGroupDeclaration.class);
    final OperationDeclaration operationDeclaration = mock(OperationDeclaration.class);
    when(operationDeclaration.getOutput()).thenReturn(mock(OutputDeclaration.class, RETURNS_DEEP_STUBS));
    when(operationDeclaration.getOutputAttributes()).thenReturn(mock(OutputDeclaration.class, RETURNS_DEEP_STUBS));
    when(operationDeclaration.getParameterGroups()).thenReturn(singletonList(parameterGroupDeclaration));
    when(extensionDeclaration.getOperations()).thenReturn(singletonList(operationDeclaration));

    ObjectFieldType paramTypeField = new ObjectFieldTypeBuilder(new MetadataFormat("label", "id")).key("key")
        .value(new ObjectTypeBuilder(new MetadataFormat("fieldLabel", "fieldKey")).build()).build();

    final Map<Class<? extends TypeAnnotation>, TypeAnnotation> typeAnnotations = new HashMap<>();
    typeAnnotations.put(TypeIdAnnotation.class, new TypeIdAnnotation("com.zernova.test.SomeClass"));
    typeAnnotations.put(ParameterDslAnnotation.class, new ParameterDslAnnotation(false, false));
    typeAnnotations.put(ClassInformationAnnotation.class,
                        new ClassInformationAnnotation("com.zernova.test.SomeClass", true, false, true, false, false,
                                                       emptyList(), null, emptyList(), false));
    final DefaultObjectType typeA = new DefaultObjectType(singletonList(paramTypeField), true, null, null, typeAnnotations);
    final DefaultObjectType typeB = new DefaultObjectType(singletonList(paramTypeField), true, null, null, typeAnnotations);
    final DefaultObjectType typeC = new DefaultObjectType(singletonList(paramTypeField), true, null, null, typeAnnotations);

    final ParameterDeclaration someParam = new ParameterDeclaration("someParam");
    someParam.setType(typeA, false);
    when(parameterGroupDeclaration.getParameters()).thenReturn(singletonList(someParam));
    when(operationDeclaration.getAllParameters()).thenReturn(singletonList(someParam));

    final Set<SubTypesModel> subTypes = new HashSet<>();
    subTypes.add(new SubTypesModel(typeB, singleton(typeC)));
    when(extensionDeclaration.getSubTypes()).thenReturn(subTypes);

    Set<ObjectType> extensionTypes = new HashSet<>();
    when(extensionDeclarer.withType(Mockito.any())).thenAnswer(inv -> {
      final ObjectType newType = inv.getArgument(0);
      extensionTypes.add(newType);
      return extensionDeclarer;
    });

    new ExtensionTypesDeclarationEnricher().enrich(extensionLoadingContext);

    assertThat("The type and subtypes are not normalized.", extensionTypes, hasItem(sameInstance(typeB)));
  }
}
