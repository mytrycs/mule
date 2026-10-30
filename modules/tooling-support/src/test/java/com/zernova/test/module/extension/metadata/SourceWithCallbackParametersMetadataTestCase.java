/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static com.zernova.runtime.api.component.location.Location.builder;
import static com.zernova.tck.junit4.matcher.metadata.MetadataKeyResultSuccessMatcher.isSuccess;
import static com.zernova.test.module.extension.metadata.MetadataExtensionFunctionalTestCase.ResolutionType.EXPLICIT_RESOLUTION;

import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.api.meta.model.source.SourceCallbackModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.api.metadata.MetadataService;
import com.zernova.runtime.api.metadata.descriptor.ComponentMetadataDescriptor;
import com.zernova.runtime.api.metadata.resolving.MetadataResult;

import org.junit.Test;

public class SourceWithCallbackParametersMetadataTestCase extends MetadataExtensionFunctionalTestCase<SourceModel> {

  public SourceWithCallbackParametersMetadataTestCase(ResolutionType resolutionType) {
    super(resolutionType);
    this.provider = resolutionType == EXPLICIT_RESOLUTION ? MetadataService::getSourceMetadata
        : (metadataService, componentId, key) -> metadataService.getSourceMetadata(componentId);
    this.location = builder().globalName(SOURCE_METADATA_WITH_CALLBACK_PARAMETERS).addSourcePart().build();
  }

  @Override
  protected String getConfigFile() {
    return METADATA_TEST;
  }

  @Test
  public void getSourceDynamicInputMetadataFromCallbackParameters() throws Exception {
    final MetadataResult<ComponentMetadataDescriptor<SourceModel>> result = getComponentDynamicMetadata(PERSON_METADATA_KEY);
    assertThat(result, isSuccess());
    ComponentMetadataDescriptor<SourceModel> componentMetadata = result.get();
    SourceModel sourceModel = componentMetadata.getModel();

    SourceCallbackModel sourceSuccessCallback = sourceModel.getSuccessCallback().get();

    assertExpectedType(getParameter(sourceSuccessCallback, RESPONSE_PARAMETER_NAME).getType(), personType);
    assertExpectedType(getParameter(sourceSuccessCallback, SUCCESS_OBJECT_PARAMETER_NAME).getType(), carType);

    SourceCallbackModel sourceErrorCallback = sourceModel.getErrorCallback().get();

    assertExpectedType(getParameter(sourceErrorCallback, RESPONSE_PARAMETER_NAME).getType(), houseType);
    assertExpectedType(getParameter(sourceErrorCallback, ERROR_OBJECT_PARAMETER_NAME).getType(), personType);
  }

  private ParameterModel getParameter(ParameterizedModel owner, String parameterName) {
    return owner.getAllParameterModels().stream().filter(parameterModel -> parameterModel.getName().equals(parameterName))
        .findFirst().orElse(null);
  }

}
