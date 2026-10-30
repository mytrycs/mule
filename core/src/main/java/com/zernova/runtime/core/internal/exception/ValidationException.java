/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import com.zernova.runtime.api.exception.ZernovaException;

/**
 * Indicates that the execution of the current event is stopped. This exception is thrown to indicate this condition to the source
 * of the flow.
 *
 * @since 4.0
 */
public class ValidationException extends ZernovaException {

  private static final long serialVersionUID = -4883468665512566232L;

}
