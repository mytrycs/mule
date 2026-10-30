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
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.metadata.TypeResolver;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.test.metadata.extension.resolver.TestMetadataResolverMetadataResolvingFailure;
import com.zernova.test.metadata.extension.resolver.TestMetadataResolverRuntimeExceptionFailure;

public class MetadataFailureOperations extends MetadataOperationsParent {

  // NamedTypeResolver throws MetadataResolvingException
  @OutputResolver(output = TestMetadataResolverMetadataResolvingFailure.class)
  @MediaType(value = ANY, strict = false)
  public Object failWithResolvingException(@Connection MetadataConnection connection,
                                           @MetadataKeyId(TestMetadataResolverMetadataResolvingFailure.class) String type,
                                           @com.zernova.sdk.api.annotation.param.Content @TypeResolver(TestMetadataResolverMetadataResolvingFailure.class) Object content) {
    return null;
  }

  // Resolver for content and output type
  // With keysResolver and KeyParam
  @OutputResolver(output = TestMetadataResolverRuntimeExceptionFailure.class)
  @MediaType(value = ANY, strict = false)
  public Object failWithRuntimeException(@Connection MetadataConnection connection,
                                         @MetadataKeyId(TestMetadataResolverRuntimeExceptionFailure.class) String type,
                                         @Content @TypeResolver(TestMetadataResolverRuntimeExceptionFailure.class) Object content) {
    return null;
  }

}
