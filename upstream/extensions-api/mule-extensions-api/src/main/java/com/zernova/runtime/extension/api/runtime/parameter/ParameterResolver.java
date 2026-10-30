/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.parameter;

import com.zernova.api.annotation.NoImplement;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.util.Optional;

/**
 * A parameter type that gives the capability of:
 *
 * <ul>
 * <li><b>Get the configured expression:</b> With {@link #getExpression()} returns the possible expression used to resolve the
 * parameter value</li>
 * <li><b>Differ the resolution:</b> Using {@link #resolve()} resolves the parameter value of type {@link T}
 * </ul>
 *
 * @param <T> The type of the value to resolve
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@NoImplement
public interface ParameterResolver<T> extends com.zernova.sdk.api.runtime.parameter.ParameterResolver<T> {
}
