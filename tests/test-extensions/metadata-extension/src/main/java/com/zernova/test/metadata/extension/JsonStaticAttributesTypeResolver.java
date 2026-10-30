/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.metadata.resolving.AttributesStaticTypeResolver;
import com.zernova.test.metadata.extension.resolver.JsonInputStaticTypeResolver;

public class JsonStaticAttributesTypeResolver extends AttributesStaticTypeResolver {

  @Override
  public MetadataType getStaticMetadata() {
    return new JsonInputStaticTypeResolver().getStaticMetadata();
  }
}
