/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.metadata;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.api.metadata.resolving.FailureCode;

/**
 * Exception that represents an error related to Metadata resolving. A {@link FailureCode} is required in order to describe the
 * error reason.
 *
 * @since 1.0
 */
public final class MetadataResolvingException extends ZernovaException {

  private final FailureCode failure;

  public MetadataResolvingException(String message, FailureCode failure) {
    super(createStaticMessage(message));
    this.failure = failure;
  }

  public MetadataResolvingException(String message, FailureCode failure, Throwable cause) {
    super(createStaticMessage(message), cause);
    this.failure = failure;
  }

  public MetadataResolvingException(I18nMessage message, FailureCode failure) {
    super(message);
    this.failure = failure;
  }

  public MetadataResolvingException(I18nMessage message, FailureCode failure, Throwable cause) {
    super(message, cause);
    this.failure = failure;
  }

  public FailureCode getFailure() {
    return failure;
  }
}
