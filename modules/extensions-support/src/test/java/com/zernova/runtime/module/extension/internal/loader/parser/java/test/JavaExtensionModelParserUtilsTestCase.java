/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.meta.model.operation.ExecutionType;
import com.zernova.runtime.extension.api.annotation.execution.Execution;
import com.zernova.runtime.module.extension.api.loader.java.type.OperationElement;
import com.zernova.runtime.module.extension.internal.loader.java.type.runtime.ClassBasedAnnotationValueFetcher;
import com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserUtils;
import com.zernova.tck.size.SmallTest;

import java.util.Optional;

import org.junit.Test;


@SmallTest
public class JavaExtensionModelParserUtilsTestCase {

  private final OperationElement operationElement = mock(OperationElement.class);
  private final ClassBasedAnnotationValueFetcher classBasedAnnotationValueFetcher = mock(ClassBasedAnnotationValueFetcher.class);

  @Test
  public void operationExecutionTypeFromLegacyAnnotation() {
    mockOperationExecutionTypeFromAnnotation(Execution.class,
                                             ExecutionType.CPU_LITE);
    Optional<ExecutionType> executionType = JavaExtensionModelParserUtils.getExecutionType(operationElement);
    assertThat(executionType.isPresent(), is(true));
    assertThat(executionType.get(), is(ExecutionType.CPU_LITE));
  }

  @Test
  public void operationExecutionTypeFromSdkApiAnnotation() {
    mockOperationExecutionTypeFromAnnotation(com.zernova.sdk.api.annotation.execution.Execution.class,
                                             com.zernova.sdk.api.runtime.operation.ExecutionType.CPU_LITE);
    Optional<ExecutionType> executionType = JavaExtensionModelParserUtils.getExecutionType(operationElement);
    assertThat(executionType.isPresent(), is(true));
    assertThat(executionType.get(), is(ExecutionType.CPU_LITE));
  }

  @Test
  public void operationExecutionTypeFromSdkApiAnnotationWithUndefinedExecutionType() {
    com.zernova.sdk.api.runtime.operation.ExecutionType UNKNOWN_EXECUTION_TYPE =
        mock(com.zernova.sdk.api.runtime.operation.ExecutionType.class);
    when(UNKNOWN_EXECUTION_TYPE.name()).thenReturn("UNKNOWN");
    mockOperationExecutionTypeFromAnnotation(com.zernova.sdk.api.annotation.execution.Execution.class,
                                             UNKNOWN_EXECUTION_TYPE);
    Optional<ExecutionType> executionType = JavaExtensionModelParserUtils.getExecutionType(operationElement);
    assertThat(executionType.isPresent(), is(false));
  }

  private void mockOperationExecutionTypeFromAnnotation(Class executionAnnotationClass, Enum executionType) {
    when(classBasedAnnotationValueFetcher.getEnumValue(any())).thenReturn(executionType);
    when(operationElement.getValueFromAnnotation(executionAnnotationClass))
        .thenReturn(Optional.of(classBasedAnnotationValueFetcher));
  }

}
