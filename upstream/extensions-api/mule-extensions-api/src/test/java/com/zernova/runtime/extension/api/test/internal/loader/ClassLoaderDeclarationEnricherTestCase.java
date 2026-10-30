/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.test.internal.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.internal.loader.enricher.ClassLoaderDeclarationEnricher;
import com.zernova.runtime.extension.api.property.ClassLoaderModelProperty;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClassLoaderDeclarationEnricherTestCase {

  @Mock
  private ExtensionLoadingContext extensionLoadingContext;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ExtensionDeclarer extensionDeclarer;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ExtensionDeclaration extensionDeclaration;

  @Mock
  private ClassLoader classLoader;

  private ClassLoaderDeclarationEnricher enricher = new ClassLoaderDeclarationEnricher();

  @Before
  public void before() {
    when(extensionLoadingContext.getExtensionDeclarer()).thenReturn(extensionDeclarer);
    when(extensionDeclarer.getDeclaration()).thenReturn(extensionDeclaration);
  }

  @Test
  public void enrich() {
    setClassLoaderParameter(classLoader);
    enricher.enrich(extensionLoadingContext);

    ArgumentCaptor<ClassLoaderModelProperty> captor = forClass(ClassLoaderModelProperty.class);
    verify(extensionDeclarer).withModelProperty(captor.capture());

    ClassLoaderModelProperty property = captor.getValue();
    assertThat(property, is(notNullValue()));
    assertThat(property.getClassLoader(), is(sameInstance(classLoader)));
  }

  @Test(expected = IllegalModelDefinitionException.class)
  public void noClassLoaderForEnrichment() {
    setClassLoaderParameter(null);
    enricher.enrich(extensionLoadingContext);
  }

  private void setClassLoaderParameter(ClassLoader classLoader) {
    when(extensionLoadingContext.getExtensionClassLoader()).thenReturn(classLoader);
  }
}
