/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.param.stereotype;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.extension.api.stereotype.StereotypeDefinition;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for {@link SourceModel Sources}, {@link OperationModel Operations}, {@link ConfigurationModel Configuration} and
 * {@link ConnectionProviderModel Connections} level to communicate and declare the {@link StereotypeDefinition}s that
 * characterize the annotated components
 *
 * @since 1.0
 * @see StereotypeDefinition
 */
@MinZernovaVersion("4.1")
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Documented
public @interface Stereotype {

  Class<? extends StereotypeDefinition> value();
}
