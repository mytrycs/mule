/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.metadata;

import static com.zernova.runtime.api.component.location.Location.builder;
import static com.zernova.runtime.api.metadata.resolving.FailureCode.INVALID_CONFIGURATION;
import static com.zernova.runtime.api.metadata.resolving.MetadataComponent.COMPONENT;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.descriptor.ComponentMetadataDescriptor;
import com.zernova.runtime.api.metadata.resolving.MetadataResult;

import org.junit.Test;

public class MetadataImplicitDynamicConfigurationTestCase extends AbstractMetadataOperationTestCase {

  public MetadataImplicitDynamicConfigurationTestCase(ResolutionType resolutionType) {
    super(resolutionType);
  }

  @Override
  protected String getConfigFile() {
    return METADATA_TEST_DYNAMIC_IMPLICIT_CONFIGURATION;
  }

  @Test
  public void resolveMetadataWithImplicitDynamicConfig() throws Exception {
    location = builder().globalName(CONTENT_METADATA_WITH_KEY_ID).addProcessorsPart().addIndexPart(0).build();
    final MetadataResult<ComponentMetadataDescriptor<OperationModel>> result = getComponentDynamicMetadata(PERSON_METADATA_KEY);
    assertFailureResult(result, 1);
    assertMetadataFailure(result.getFailures().get(0),
                          "Configuration used for Metadata fetch cannot be dynamic",
                          INVALID_CONFIGURATION,
                          MetadataResolvingException.class.getName(),
                          COMPONENT,
                          "");
  }
}
