/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.manager;

import static com.zernova.metadata.api.builder.BaseTypeBuilder.create;
import static com.zernova.metadata.java.api.JavaTypeLoader.JAVA;
import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;
import static com.zernova.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.TYPE_PROPERTY_NAME;
import static com.zernova.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.VERSION;
import static com.zernova.tck.util.ZernovaContextUtils.mockMuleContext;
import static com.zernova.tck.util.ZernovaContextUtils.verifyRegistration;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Optional.empty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.metadata.api.annotation.TypeAliasAnnotation;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.el.ExpressionModule;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.privileged.el.GlobalBindingContextProvider;
import com.zernova.runtime.module.extension.internal.loader.java.DefaultJavaExtensionModelLoader;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.test.heisenberg.extension.HeisenbergExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.mockito.ArgumentCaptor;

import io.qameta.allure.Issue;

@SmallTest
public class ExtensionActivatorTestCase extends AbstractZernovaTestCase {

  private static final String MOCK_EXTENSION_PREFIX = "mock";
  private static final String MOCK_TYPE_ALIAS = "MockType";
  private static final String MOCK_TYPE_ID = "MockTypeId";
  private static final String MOCK_EXTENSION_NAME = "Mock Extension";

  @Test
  public void enumsReleasedWhenStopped() throws Exception {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put(TYPE_PROPERTY_NAME, HeisenbergExtension.class.getName());
    attributes.put(VERSION, "1.0.0");
    // TODO MULE-14517: This workaround should be replaced for a better and more complete mechanism
    attributes.put("COMPILATION_MODE", true);

    ExtensionModel extensionModel =
        new DefaultJavaExtensionModelLoader().loadExtensionModel(HeisenbergExtension.class.getClassLoader(),
                                                                 getDefault(emptySet()),
                                                                 attributes);

    ExtensionActivator extensionActivator = new ExtensionActivator(mockMuleContext());
    extensionActivator.activateExtension(extensionModel);
    assertThat(extensionActivator.getEnumTypes().size(), is(greaterThan(0)));

    extensionActivator.stop();
    assertThat(extensionActivator.getEnumTypes(), hasSize(0));
  }

  @Test
  @Issue("W-11969246")
  public void activatedExtensionTypesAreAddedToGlobalBindings() throws ZernovaException {
    // Given a type
    ObjectType mockType = create(JAVA).objectType()
        .id(MOCK_TYPE_ID)
        .with(new TypeAliasAnnotation(MOCK_TYPE_ALIAS))
        .build();

    // Given an extension model declaring that type
    ExtensionModel extensionModel = extensionWithTypes(singleton(mockType));

    // Given a mule context
    ZernovaContext muleContext = mockMuleContext();

    // When the extension activator activates that extension model
    ExtensionActivator extensionActivator = new ExtensionActivator(muleContext);
    extensionActivator.activateExtension(extensionModel);

    // Then a global binding context is registered in the mule context registry, and it contains the type
    ArgumentCaptor<GlobalBindingContextProvider> bcProviderCaptor = forClass(GlobalBindingContextProvider.class);
    String registryKey = MOCK_EXTENSION_NAME + "GlobalBindingContextProvider";
    verifyRegistration(muleContext, registryKey, bcProviderCaptor);
    BindingContext bindingContext = bcProviderCaptor.getValue().getBindingContext();

    assertThat(bindingContext.modules().size(), is(1));
    ExpressionModule module = bindingContext.modules().stream().findAny().get();
    assertThat(module.declaredTypes(), contains(mockType));
  }

  private static ExtensionModel extensionWithTypes(Set<ObjectType> metadataTypes) {
    ExtensionModel mockExtensionModel = mock(ExtensionModel.class, RETURNS_MOCKS);
    XmlDslModel dslModel = XmlDslModel.builder().setPrefix(MOCK_EXTENSION_PREFIX).build();
    when(mockExtensionModel.getXmlDslModel()).thenReturn(dslModel);
    when(mockExtensionModel.getName()).thenReturn(MOCK_EXTENSION_NAME);
    when(mockExtensionModel.getTypes()).thenReturn(metadataTypes);
    when(mockExtensionModel.getModelProperty(any())).thenReturn(empty());
    return mockExtensionModel;
  }
}
