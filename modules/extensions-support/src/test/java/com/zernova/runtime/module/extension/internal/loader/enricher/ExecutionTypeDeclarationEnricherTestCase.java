/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.enricher;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.meta.model.operation.ExecutionType.BLOCKING;
import static com.zernova.runtime.api.meta.model.operation.ExecutionType.CPU_INTENSIVE;
import static com.zernova.runtime.api.meta.model.operation.ExecutionType.CPU_LITE;
import com.zernova.runtime.api.meta.model.operation.ExecutionType;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.internal.loader.enricher.ExecutionTypeDeclarationEnricher;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ExecutionTypeDeclarationEnricherTestCase extends AbstractZernovaTestCase {

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ExtensionLoadingContext extensionLoadingContext;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ExtensionDeclarer extensionDeclarer;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ExtensionDeclaration extensionDeclaration;

  private OperationDeclaration operation = new OperationDeclaration("operation");

  private ExecutionTypeDeclarationEnricher enricher = new ExecutionTypeDeclarationEnricher();

  @Before
  public void before() {
    when(extensionLoadingContext.getExtensionDeclarer()).thenReturn(extensionDeclarer);
    when(extensionDeclarer.getDeclaration()).thenReturn(extensionDeclaration);
    when(extensionDeclaration.getOperations()).thenReturn(asList(operation));
  }

  @Test
  public void operationSpecifiesExecutionType() {
    operation.setExecutionType(CPU_INTENSIVE);
    enrichAndExpect(CPU_INTENSIVE);
  }

  @Test
  public void blockingConnectedOperation() {
    operation.setBlocking(true);
    operation.setRequiresConnection(true);

    enrichAndExpect(BLOCKING);
  }

  @Test
  public void nonBlockingConnectedOperation() {
    operation.setBlocking(false);
    operation.setRequiresConnection(true);

    enrichAndExpect(CPU_LITE);
  }

  @Test
  public void nonConnectedOperation() {
    operation.setBlocking(true);
    operation.setRequiresConnection(false);

    enrichAndExpect(CPU_LITE);
  }

  private void enrichAndExpect(ExecutionType type) {
    enricher.enrich(extensionLoadingContext);
    assertThat(operation.getExecutionType(), is(type));
  }

}
