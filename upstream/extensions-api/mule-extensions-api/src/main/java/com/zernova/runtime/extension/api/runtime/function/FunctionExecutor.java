/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.function;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.el.ExpressionFunction;

/**
 * A facade interface which hides the details of how a function is actually executed. It aims to decouple the abstract
 * introspection model that the extension's API proposes from the implementation details of the underlying environment.
 *
 * @since 1.10.0
 */
@NoImplement
public interface FunctionExecutor extends ExpressionFunction {

}
