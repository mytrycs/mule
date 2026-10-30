/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.object;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;

/**
 * Specialization of {@link ParsingDelegate} that always produces instances of {@link ValueResolver}
 *
 * @since 4.0
 */
public interface ValueResolverParsingDelegate extends ParsingDelegate<MetadataType, ValueResolver<Object>> {

}
