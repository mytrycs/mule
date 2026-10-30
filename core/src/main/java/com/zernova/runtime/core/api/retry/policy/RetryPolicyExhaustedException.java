/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.retry.policy;

import com.zernova.runtime.core.api.lifecycle.FatalException;
import com.zernova.runtime.api.i18n.I18nMessage;

/**
 * This exception is thrown when a Retry policy has made all the retry attempts it wants to make and is still failing.
 */
public final class RetryPolicyExhaustedException extends FatalException {

  /** Serial version */
  private static final long serialVersionUID = 3300563235465630597L;

  public RetryPolicyExhaustedException(I18nMessage message, Object component) {
    super(message, component);
  }

  public RetryPolicyExhaustedException(I18nMessage message, Throwable cause, Object component) {
    super(message, cause, component);
  }

  public RetryPolicyExhaustedException(Throwable cause, Object component) {
    super(cause, component);
  }
}
