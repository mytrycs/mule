/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.exception;

import static com.google.common.collect.Iterables.getLast;
import static java.util.Collections.emptyList;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.exception.ComposedErrorException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.api.message.Error;

import java.util.List;

public class MessageRedeliveredException extends ZernovaException implements ComposedErrorException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = 9013890402770563933L;

  private final String messageId;
  private final List<Error> errors;
  private final int redeliveryCount;
  private final int maxRedelivery;

  protected MessageRedeliveredException(String messageId, List<Error> errors, int redeliveryCount, int maxRedelivery,
                                        I18nMessage message, Throwable cause) {
    super(message, cause);
    this.messageId = messageId;
    this.errors = errors;
    this.redeliveryCount = redeliveryCount;
    this.maxRedelivery = maxRedelivery;
  }

  public MessageRedeliveredException(String messageId, int redeliveryCount, int maxRedelivery, Throwable cause) {
    this(messageId, emptyList(), redeliveryCount, maxRedelivery,
         createStaticMessage("%s caught while handling redelivery: %s", cause.getClass().getName(), cause.getMessage()),
         cause);
  }

  public MessageRedeliveredException(String messageId, int redeliveryCount, int maxRedelivery) {
    this(messageId, emptyList(), redeliveryCount, maxRedelivery, createStaticMessage("Maximum redelivery attempts reached"),
         null);
  }

  public MessageRedeliveredException(String messageId, List<Error> errors, int redeliveryCount, int maxRedelivery) {
    this(messageId, errors, redeliveryCount, maxRedelivery,
         createStaticMessage("Maximum redelivery attempts reached. Last error was %s.",
                             getLast(errors).getErrorType().toString()),
         null);
  }

  public String getMessageId() {
    return messageId;
  }

  @Override
  public List<Error> getErrors() {
    return errors;
  }

  public int getRedeliveryCount() {
    return redeliveryCount;
  }

  public int getMaxRedelivery() {
    return maxRedelivery;
  }
}
