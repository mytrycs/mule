/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.validation;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getApiMethods;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.TYPE_LOADER;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.validate;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.runtime.streaming.PagingProvider;
import com.zernova.runtime.extension.internal.property.PagedOperationModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionOperationDescriptorModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.runtime.OperationWrapper;
import com.zernova.runtime.module.extension.internal.loader.java.validation.PagedOperationModelValidator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class PagedOperationModelValidatorTestCase extends AbstractZernovaTestCase {

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ExtensionModel extensionModel;

  @Mock
  private OperationModel operationModel;

  private ExtensionModelValidator validator = new PagedOperationModelValidator();

  @Before
  public void before() {
    when(extensionModel.getOperationModels()).thenReturn(asList(operationModel));
    when(operationModel.getModelProperty(PagedOperationModelProperty.class))
        .thenReturn(Optional.of(new PagedOperationModelProperty()));
    when(extensionModel.getImportedTypes()).thenReturn(emptySet());
  }

  @Test(expected = IllegalModelDefinitionException.class)
  public void connectedPagingProvider() {
    mockOperationModel("connectedPagingProvider");
    validate(extensionModel, validator);
  }

  @Test
  public void notConnectedPagingProvider() {
    mockOperationModel("notConnectedPagingProvider");
    validate(extensionModel, validator);
  }

  private void mockOperationModel(String method) {
    when(operationModel.getModelProperty(ExtensionOperationDescriptorModelProperty.class))
        .thenReturn(getApiMethods(PagedOperations.class).stream()
            .filter(m -> m.getName().equals(method))
            .findFirst()
            .map(pagedMethod -> new ExtensionOperationDescriptorModelProperty(new OperationWrapper(pagedMethod, TYPE_LOADER))));
  }

  private static class PagedOperations {

    public PagingProvider<Object, Object> connectedPagingProvider(@Connection Object connection) {
      return null;
    }

    public PagingProvider<Object, Object> notConnectedPagingProvider() {
      return null;
    }
  }

}
