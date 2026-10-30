/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.metadata;

import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.metadata.MetadataService;

public abstract class AbstractMetadataOperationTestCase extends MetadataExtensionFunctionalTestCase<OperationModel> {

  public AbstractMetadataOperationTestCase(ResolutionType resolutionType) {
    super(resolutionType);
    this.provider = resolutionType == ResolutionType.EXPLICIT_RESOLUTION ? MetadataService::getOperationMetadata
        : (metadataService, componentId, key) -> metadataService.getOperationMetadata(componentId);
  }
}
