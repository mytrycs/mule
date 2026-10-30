/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.el;

import static com.zernova.test.allure.AllureConstants.ExpressionLanguageFeature.EXPRESSION_LANGUAGE;
import static com.zernova.test.allure.AllureConstants.ExpressionLanguageFeature.ExpressionLanguageStory.SUPPORT_EXPRESSION_BINDINGS;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;

import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataFormat;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.el.ExpressionModule;
import com.zernova.runtime.api.el.ModuleNamespace;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.weave.v2.interpreted.node.executors.FunctionExecutor;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Issue("W-12412432")
@Feature(EXPRESSION_LANGUAGE)
@Story(SUPPORT_EXPRESSION_BINDINGS)
public class CompositeExpressionModuleTestCase extends AbstractZernovaTestCase {

  @Rule
  public ExpectedException expectedException = none();

  @Test
  public void namespaceOfTheCompositeIsTheNamespaceOfBothSingle() {
    ModuleNamespace namespace = new ModuleNamespace("Namespace");
    ExpressionModule m1 = new DefaultExpressionModuleBuilder(namespace).build();
    ExpressionModule m2 = new DefaultExpressionModuleBuilder(namespace).build();
    ExpressionModule m3 = new DefaultExpressionModuleBuilder(namespace).build();
    ExpressionModule expressionModule = new CompositeExpressionModule(m1, m2, m3);
    assertThat(expressionModule.namespace(), is(namespace));
  }

  @Test
  public void canNotBuildCompositeIfNamespaceDiffers() {
    ModuleNamespace namespace1 = new ModuleNamespace("Namespace1");
    ModuleNamespace namespace2 = new ModuleNamespace("Namespace2");
    ExpressionModule m1 = new DefaultExpressionModuleBuilder(namespace1).build();
    ExpressionModule m2 = new DefaultExpressionModuleBuilder(namespace2).build();

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Expected unique namespace but found multiple [Namespace1, Namespace2]");
    new CompositeExpressionModule(m1, m2);
  }

  @Test
  public void modulesWithDifferentBindingAreMerged() {
    MetadataType someType = BaseTypeBuilder.create(MetadataFormat.JAVA).stringType().build();
    TypedValue<FunctionExecutor> someFunction = new TypedValue<>(mock(FunctionExecutor.class), mock(DataType.class));
    ModuleNamespace namespace = new ModuleNamespace("Namespace");

    ExpressionModule moduleWithType = new DefaultExpressionModuleBuilder(namespace)
        .addType(someType)
        .build();

    ExpressionModule moduleWithFunction = new DefaultExpressionModuleBuilder(namespace)
        .addBinding("someFunction", someFunction)
        .build();

    ExpressionModule merged = new CompositeExpressionModule(moduleWithFunction, moduleWithType);
    assertThat(merged.declaredTypes(), contains(someType));
    assertThat(merged.lookup("someFunction").get(), is(someFunction));
  }
}
