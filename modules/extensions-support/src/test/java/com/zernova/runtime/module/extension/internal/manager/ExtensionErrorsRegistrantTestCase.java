/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.manager;

import static com.zernova.runtime.api.component.ComponentIdentifier.builder;
import static com.zernova.runtime.api.meta.model.error.ErrorModelBuilder.newError;
import static com.zernova.runtime.api.util.NameUtils.hyphenize;
import static com.zernova.runtime.core.api.error.Errors.Identifiers.CONNECTIVITY_ERROR_IDENTIFIER;
import static com.zernova.runtime.core.api.error.Errors.Identifiers.RETRY_EXHAUSTED_ERROR_IDENTIFIER;
import static com.zernova.runtime.core.internal.exception.ErrorTypeLocatorFactory.createDefaultErrorTypeLocator;
import static com.zernova.runtime.module.extension.internal.manager.ExtensionErrorsRegistrant.registerErrorMappings;
import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServices;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.visitableMock;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.error.ErrorModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.message.ErrorTypeBuilder;
import com.zernova.runtime.core.privileged.PrivilegedZernovaContext;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import io.qameta.allure.Issue;

@SmallTest
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class ExtensionErrorsRegistrantTestCase extends AbstractZernovaTestCase {

  private static final String TEST_EXTENSION_NAME = "Test Extension";
  private static final String EXTENSION_PREFIX = "test-namespace";
  private static final String ERROR_PREFIX = EXTENSION_PREFIX.toUpperCase();
  private static final String OPERATION_NAME = "operationName";
  private static final String TEST_CONNECTIVITY_ERROR_TYPE = "TEST_CONNECTIVITY";
  private static final String ZERNOVA = "ZERNOVA";
  private static final String ANY = "ANY";

  private static final ComponentIdentifier OPERATION_IDENTIFIER = builder()
      .name(hyphenize(OPERATION_NAME))
      .namespace(EXTENSION_PREFIX)
      .build();

  private static final ErrorModel MULE_CONNECTIVITY_ERROR =
      newError(CONNECTIVITY_ERROR_IDENTIFIER, ZERNOVA)
          .build();

  private static final ErrorModel extensionConnectivityError =
      newError(TEST_CONNECTIVITY_ERROR_TYPE, ERROR_PREFIX)
          .withParent(MULE_CONNECTIVITY_ERROR)
          .build();

  @Mock
  private ExtensionModel extensionModel;

  @Mock
  private OperationModel operationWithError;

  @Mock
  private OperationModel operationWithoutErrors;

  private final ZernovaContext muleContext = mockContextWithServices();
  private ErrorTypeRepository typeRepository;
  private ErrorTypeLocator typeLocator;

  @BeforeEach
  public void setUp() {
    XmlDslModel.XmlDslModelBuilder builder = XmlDslModel.builder();
    builder.setPrefix(EXTENSION_PREFIX);
    XmlDslModel xmlDslModel = builder.build();

    final ErrorType anyErrorType = ErrorTypeBuilder.builder()
        .namespace(ZERNOVA)
        .identifier(ANY)
        .build();

    typeRepository = mock(ErrorTypeRepository.class);
    when(typeRepository.getErrorType(any())).thenReturn(of(mock(ErrorType.class)));
    when(typeRepository.lookupErrorType(any())).thenReturn(of(mock(ErrorType.class)));
    when(typeRepository.getCriticalErrorType()).thenReturn(mock(ErrorType.class));
    when(typeRepository.getAnyErrorType()).thenReturn(anyErrorType);
    typeLocator = createDefaultErrorTypeLocator(typeRepository);

    when(muleContext.getErrorTypeRepository()).thenReturn(typeRepository);
    when(((PrivilegedZernovaContext) muleContext).getErrorTypeLocator()).thenReturn(typeLocator);

    when(extensionModel.getXmlDslModel()).thenReturn(xmlDslModel);
    when(extensionModel.getName()).thenReturn(TEST_EXTENSION_NAME);

    when(operationWithError.getErrorModels()).thenReturn(singleton(extensionConnectivityError));
    when(operationWithError.getName()).thenReturn(OPERATION_NAME);
    when(operationWithError.getModelProperty(any())).thenReturn(empty());

    when(operationWithoutErrors.getName()).thenReturn("operationWithoutError");
    when(operationWithoutErrors.getErrorModels()).thenReturn(emptySet());
    when(operationWithoutErrors.getModelProperty(any())).thenReturn(empty());

    visitableMock(operationWithError, operationWithoutErrors);
  }

  @Test
  void lookupErrorsForOperation() {
    when(extensionModel.getOperationModels()).thenReturn(asList(operationWithError, operationWithoutErrors));

    doTest();
  }

  @Test
  @Issue("MULE-19293")
  void lookupErrorsForOperationFromConfig() {
    final ConfigurationModel configModel = mock(ConfigurationModel.class);
    when(configModel.getOperationModels()).thenReturn(asList(operationWithError, operationWithoutErrors));
    when(extensionModel.getConfigurationModels()).thenReturn(singletonList(configModel));

    doTest();
  }

  protected void doTest() {
    when(extensionModel.getErrorModels()).thenReturn(singleton(extensionConnectivityError));

    final ErrorType extConnectivityErrorType = ErrorTypeBuilder.builder()
        .namespace(ERROR_PREFIX)
        .identifier(CONNECTIVITY_ERROR_IDENTIFIER)
        .parentErrorType(ErrorTypeBuilder.builder()
            .namespace(ZERNOVA)
            .identifier(CONNECTIVITY_ERROR_IDENTIFIER)
            .parentErrorType(typeRepository.getAnyErrorType())
            .build())
        .build();

    when(typeRepository.lookupErrorType(builder().namespace(ERROR_PREFIX).name(CONNECTIVITY_ERROR_IDENTIFIER).build()))
        .thenReturn(of(extConnectivityErrorType));
    when(typeRepository.lookupErrorType(builder().namespace(ERROR_PREFIX).name(RETRY_EXHAUSTED_ERROR_IDENTIFIER).build()))
        .thenReturn(empty());

    registerErrorMappings(typeRepository, typeLocator, singleton(extensionModel), em -> empty());

    ErrorType errorType = typeLocator.lookupComponentErrorType(OPERATION_IDENTIFIER, ConnectionException.class);

    assertThat(errorType.getIdentifier(), is(CONNECTIVITY_ERROR_IDENTIFIER));
    assertThat(errorType.getNamespace(), is(EXTENSION_PREFIX.toUpperCase()));

    ErrorType muleConnectivityError = errorType.getParentErrorType();
    assertThat(muleConnectivityError.getNamespace(), is(MULE_CONNECTIVITY_ERROR.getNamespace()));
    assertThat(muleConnectivityError.getIdentifier(), is(MULE_CONNECTIVITY_ERROR.getType()));

    ErrorType anyErrorType = muleConnectivityError.getParentErrorType();
    assertThat(anyErrorType.getNamespace(), is(ZERNOVA));
    assertThat(anyErrorType.getIdentifier(), is(ANY));

    assertThat(anyErrorType.getParentErrorType(), is(nullValue()));
  }

}
