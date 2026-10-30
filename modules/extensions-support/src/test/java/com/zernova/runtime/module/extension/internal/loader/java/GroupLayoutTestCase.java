/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static com.zernova.test.vegan.extension.SpreadVeganismOperation.ARGUMENTS_TAB;

import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclaration;
import com.zernova.test.vegan.extension.VeganExtension;

import org.junit.Before;
import org.junit.Test;

public class GroupLayoutTestCase extends AbstractJavaExtensionDeclarationTestCase {

  private ExtensionDeclaration extensionDeclaration;

  @Before
  public void setUp() {
    extensionDeclaration = declarerFor(VeganExtension.class).getDeclaration();
  }

  @Test
  public void groupLayout() {
    OperationDeclaration operation = getOperation(extensionDeclaration, "convinceAnimalKiller");
    assertThat(operation.getParameterGroups(), hasSize(2));
    ParameterGroupDeclaration group = operation.getParameterGroups().get(0);
    assertThat(group.getLayoutModel().getTabName().get(), is(ARGUMENTS_TAB));
  }

}
