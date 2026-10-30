/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.exception;

import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcherUtils;

/**
 * Decides whether an error type is acceptable.
 *
 * @since 4.0
 * @deprecated use {@link com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcher} instead.
 */
@Deprecated
public interface ErrorTypeMatcher extends com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcher {

}
