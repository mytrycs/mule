/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;

/**
 * An {@link ArgumentResolver} which returns the {@link MediaType} of the current message
 *
 * @since 4.0
 */
public final class MediaTypeArgumentResolver implements ArgumentResolver<MediaType> {

  @Override
  public MediaType resolve(ExecutionContext executionContext) {
    return ((ExecutionContextAdapter) executionContext).getEvent().getMessage().getPayload().getDataType().getMediaType();
  }
}
