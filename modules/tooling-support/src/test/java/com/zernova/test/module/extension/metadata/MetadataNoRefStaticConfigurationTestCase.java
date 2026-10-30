/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.metadata;

import static com.zernova.runtime.api.component.location.Location.builder;

import org.junit.Test;

public class MetadataNoRefStaticConfigurationTestCase extends AbstractMetadataOperationTestCase {

  public MetadataNoRefStaticConfigurationTestCase(ResolutionType resolutionType) {
    super(resolutionType);
  }

  @Override
  protected String getConfigFile() {
    return METADATA_TEST_STATIC_NO_REF_CONFIGURATION;
  }

  @Test
  public void resolveMetadataWithNoRefStaticConfig() throws Exception {
    location = builder().globalName(RESOLVER_WITH_IMPLICIT_STATIC_CONFIG).addProcessorsPart()
        .addIndexPart(0).build();
    getSuccessComponentDynamicMetadata(LOCATION_MULTILEVEL_KEY);
  }
}
