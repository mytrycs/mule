/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.metadata;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.resolving.QueryEntityResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.Query;
import com.zernova.runtime.extension.api.metadata.NullMetadataResolver;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks one of the parameters of an Operation or Source as the key for the metadata lookup. This lookup is resolved by the
 * {@link TypeKeysResolver} referenced in the {@link MetadataScope}, at Operation, Source or Extension level, which will return
 * the list of available the {@link MetadataKey} or also, the lookup can be resolved by the
 * {@link QueryEntityResolver#getEntityKeys(MetadataContext)} for {@link Query} annotated operations, this time the key will
 * represent the query (DSQL or Native) which is used to calculate the output metadata.
 * <p>
 * <b>Annotation Usage:</b>
 * <ul>
 * <li><b>Operations: </b> The annotation must be applied to an Operation parameter to mark it as the metadata key parameter. The
 * usage of this annotation in multiple parameters of the same operation or fields of the operation class is not allowed.</li>
 * <li><b>Sources: </b> The annotation must be applied to a Source field annotated with {@link Parameter} to mark it as the
 * metadata key parameter. The usage of this annotation in a non {@link Parameter} field, multiple fields or in a method
 * parameter, is not allowed.</li>
 * </ul>
 * The illegal usage of this annotation could produce extension compilation errors
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
public @interface MetadataKeyId {

  /**
   * @return the associated {@link TypeKeysResolver} for the annotated Component
   */
  Class<? extends TypeKeysResolver> value() default NullMetadataResolver.class;

}
