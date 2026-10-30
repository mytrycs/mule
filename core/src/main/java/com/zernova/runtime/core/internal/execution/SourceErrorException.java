/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.execution;

import static java.util.Optional.ofNullable;
import static com.zernova.runtime.api.notification.EnrichedNotificationInfo.createInfo;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.notification.EnrichedNotificationInfo;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.execution.ExceptionContextProvider;
import com.zernova.runtime.core.api.source.MessageSource;
import com.zernova.runtime.core.internal.message.ErrorBuilder;
import com.zernova.runtime.core.privileged.exception.MessagingException;

import java.util.Collection;
import java.util.Optional;

/**
 * Represents an error that happens when a Source is processing the response of a flow.
 *
 * @since 4.0
 */
public final class SourceErrorException extends ZernovaRuntimeException {

  private static final long serialVersionUID = 160215774280116878L;

  private final CoreEvent event;
  private final ErrorType errorType;
  private final MessagingException originalCause;

  public SourceErrorException(CoreEvent event, ErrorType errorType, Throwable cause) {
    super(cause);
    this.event = event;
    this.errorType = errorType;
    this.originalCause = null;
  }

  public SourceErrorException(CoreEvent event, ErrorType errorType, Throwable cause, MessagingException originalCause) {
    super(cause);
    this.event = event;
    this.errorType = errorType;
    this.originalCause = originalCause;
  }

  public CoreEvent getEvent() {
    return event;
  }

  public ErrorType getErrorType() {
    return errorType;
  }

  public Optional<MessagingException> getOriginalCause() {
    return ofNullable(originalCause);
  }

  public MessagingException toMessagingException(Collection<ExceptionContextProvider> exceptionContextProviders,
                                                 MessageSource messageSource) {
    MessagingException messagingException = new MessagingException(CoreEvent.builder(getEvent())
        .error(ErrorBuilder.builder(getCause())
            .errorType(getErrorType())
            .build())
        .build(), getCause());

    EnrichedNotificationInfo notificationInfo = createInfo(messagingException.getEvent(), messagingException, null);
    for (ExceptionContextProvider exceptionContextProvider : exceptionContextProviders) {
      exceptionContextProvider.putContextInfo(messagingException.getExceptionInfo(), notificationInfo, messageSource);
    }

    return messagingException;
  }

}
