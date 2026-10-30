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
import static com.zernova.runtime.api.meta.ExpressionSupport.REQUIRED;
import static com.zernova.runtime.api.meta.ExpressionSupport.SUPPORTED;
import static com.zernova.runtime.api.meta.model.parameter.ParameterRole.BEHAVIOUR;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.mockParameters;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.mockSubTypes;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.toMetadataType;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.validate;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.ParameterDslConfiguration;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthParameterModelProperty;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.property.InfrastructureParameterModelProperty;
import com.zernova.runtime.extension.api.property.QNameModelProperty;
import com.zernova.runtime.module.extension.internal.loader.validator.ParameterPluralNameModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.property.ParameterGroupModelProperty;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ParameterPluralNameModelValidatorTestCase extends AbstractZernovaTestCase {

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  private ExtensionModel extensionModel;

  @Mock(lenient = true)
  private OperationModel operationModel;

  @Mock(lenient = true)
  private ParameterModel validParameterModel;

  @Mock(lenient = true)
  private ParameterModel invalidParameterModel;

  private ParameterPluralNameModelValidator validator = new ParameterPluralNameModelValidator();

  @Before
  public void before() {
    when(extensionModel.getOperationModels()).thenReturn(asList(operationModel));
    mockSubTypes(extensionModel);
    when(extensionModel.getName()).thenReturn("extensionModel");
    when(extensionModel.getImportedTypes()).thenReturn(emptySet());
    when(extensionModel.getXmlDslModel())
        .thenReturn(XmlDslModel.builder().setPrefix("ns").setNamespace("https://schemas.zernova.dev/ns")
            .setSchemaLocation("https://schemas.zernova.dev/heisenberg/current/zernova-ns.xsd").build());

    when(operationModel.getName()).thenReturn("dummyOperation");

    when(validParameterModel.getModelProperty(ParameterGroupModelProperty.class)).thenReturn(Optional.empty());
    when(validParameterModel.getModelProperty(QNameModelProperty.class)).thenReturn(Optional.empty());
    when(validParameterModel.getModelProperty(InfrastructureParameterModelProperty.class)).thenReturn(Optional.empty());
    when(validParameterModel.getModelProperty(OAuthParameterModelProperty.class)).thenReturn(Optional.empty());
    when(validParameterModel.getDslConfiguration()).thenReturn(ParameterDslConfiguration.getDefaultInstance());
    when(validParameterModel.getRole()).thenReturn(BEHAVIOUR);
    when(validParameterModel.getLayoutModel()).thenReturn(Optional.empty());

    when(invalidParameterModel.getModelProperty(ParameterGroupModelProperty.class)).thenReturn(Optional.empty());
    when(invalidParameterModel.getModelProperty(QNameModelProperty.class)).thenReturn(Optional.empty());
    when(invalidParameterModel.getModelProperty(InfrastructureParameterModelProperty.class)).thenReturn(Optional.empty());
    when(invalidParameterModel.getModelProperty(OAuthParameterModelProperty.class)).thenReturn(Optional.empty());
    when(invalidParameterModel.getDslConfiguration()).thenReturn(ParameterDslConfiguration.getDefaultInstance());
    when(invalidParameterModel.getRole()).thenReturn(BEHAVIOUR);
    when(invalidParameterModel.getLayoutModel()).thenReturn(Optional.empty());
    when(invalidParameterModel.getExpressionSupport()).thenReturn(SUPPORTED);
  }

  @Test(expected = IllegalModelDefinitionException.class)
  public void invalidModelDueToListWithoutPluralName() {
    when(invalidParameterModel.getType()).thenReturn(toMetadataType(List.class));
    when(invalidParameterModel.getName()).thenReturn("thing");
    mockParameters(operationModel, invalidParameterModel);
    validate(extensionModel, validator);
  }

  public void isValidNameIfDoesntSupportChildElement() {
    when(invalidParameterModel.getType()).thenReturn(toMetadataType(List.class));
    when(invalidParameterModel.getName()).thenReturn("thing");
    when(invalidParameterModel.getDslConfiguration()).thenReturn(ParameterDslConfiguration.builder()
        .allowsInlineDefinition(false)
        .allowTopLevelDefinition(false)
        .build());
    mockParameters(operationModel, invalidParameterModel);
    validate(extensionModel, validator);
  }

  public void isValidNameIfRequiresExpression() {
    when(invalidParameterModel.getType()).thenReturn(toMetadataType(List.class));
    when(invalidParameterModel.getName()).thenReturn("thing");
    when(invalidParameterModel.getExpressionSupport()).thenReturn(REQUIRED);
    mockParameters(operationModel, invalidParameterModel);
    validate(extensionModel, validator);
  }

}
