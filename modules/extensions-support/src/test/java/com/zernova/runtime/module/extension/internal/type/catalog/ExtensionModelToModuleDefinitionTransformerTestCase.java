/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.type.catalog;

import static com.zernova.metadata.api.builder.BaseTypeBuilder.create;
import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.test.allure.AllureConstants.ReuseFeature.REUSE;
import static com.zernova.test.allure.AllureConstants.ReuseFeature.ReuseStory.TYPES_CATALOG;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.metadata.api.annotation.TypeAliasAnnotation;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.message.api.el.ModuleDefinition;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.Set;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Test;

@Feature(REUSE)
@Story(TYPES_CATALOG)
@Issue("W-11706194")
public class ExtensionModelToModuleDefinitionTransformerTestCase extends AbstractZernovaTestCase {

  private static final String MOCK_EXTENSION_PREFIX = "mock";
  private static final String MOCK_TYPE_ALIAS = "MockType";
  private static final String MOCK_TYPE_ID = "MockTypeId";
  private static final String MOCK_EXTENSION_NAME = "Mock Extension";

  private ObjectType mockType;
  private ExtensionModel extensionModel;
  private ExtensionModelToModuleDefinitionTransformer extensionModelToModuleDefinitionTransformer;

  @Before
  public void setUp() {
    mockType = create(JAVA).objectType()
        .id(MOCK_TYPE_ID)
        .with(new TypeAliasAnnotation(MOCK_TYPE_ALIAS))
        .build();
    extensionModel = extensionWithTypes(asSet(mockType));
    extensionModelToModuleDefinitionTransformer = new ExtensionModelToModuleDefinitionTransformer();
  }

  private static ExtensionModel extensionWithTypes(Set<ObjectType> metadataTypes) {
    ExtensionModel mockExtensionModel = mock(ExtensionModel.class);
    XmlDslModel dslModel = XmlDslModel.builder().setPrefix(MOCK_EXTENSION_PREFIX).build();
    when(mockExtensionModel.getXmlDslModel()).thenReturn(dslModel);
    when(mockExtensionModel.getName()).thenReturn(MOCK_EXTENSION_NAME);
    when(mockExtensionModel.getTypes()).thenReturn(metadataTypes);
    return mockExtensionModel;
  }

  @Test
  public void resultingModuleDefinitionHasExpectedType() {
    ModuleDefinition moduleDefinition = extensionModelToModuleDefinitionTransformer.apply(extensionModel);
    assertThat(moduleDefinition.declaredTypes(), contains(mockType));
  }

  private static <T> Set<T> asSet(T... a) {
    return stream(a).collect(toSet());
  }
}
