/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.utils;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.i18n.I18nMessage;

/**
 * Exception which indicates that an attempt to add a {@link com.zernova.runtime.http.api.server.RequestMatcher} has resulted in a
 * collision with a previously stored matcher.
 *
 * @since 4.1.5
 */
public class MatcherCollisionException extends ZernovaRuntimeException {

  public MatcherCollisionException(I18nMessage message) {
    super(message);
  }
}
