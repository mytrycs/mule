/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.ANY;

import com.zernova.runtime.extension.api.annotation.metadata.MetadataKeyId;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;

public class MetadataInheritedOperationResolversOperations extends MetadataOperationsParent {

  @MediaType(value = ANY, strict = false)
  public Object shouldInheritOperationParentResolvers(@com.zernova.sdk.api.annotation.param.Connection MetadataConnection connection,
                                                      @MetadataKeyId String type,
                                                      @Optional @Content Object content) {
    return null;
  }

}
