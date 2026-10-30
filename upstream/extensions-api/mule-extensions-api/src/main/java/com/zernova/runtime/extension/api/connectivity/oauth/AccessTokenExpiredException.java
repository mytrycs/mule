/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.connectivity.oauth;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Exception to be thrown by operations which are trying to use an expired access token.
 * <p>
 * The runtime will automatically catch this exception, try to execute the refresh token flow and retry the operation. If the
 * operation fails again or the token couldn't be refresh for whatever reason, then the operation will fail.
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
public class AccessTokenExpiredException extends com.zernova.sdk.api.connectivity.oauth.AccessTokenExpiredException {

  /**
   * {@inheritDoc}
   *
   * @since 1.2.1
   */
  public AccessTokenExpiredException() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  public AccessTokenExpiredException(String resourceOwnerId) {
    super(resourceOwnerId);
  }

}
