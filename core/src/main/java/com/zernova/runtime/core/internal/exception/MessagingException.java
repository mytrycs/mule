/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.core.api.event.CoreEvent;



/**
 * <code>MessagingException</code> is a general message exception thrown when errors specific to Message processing occur..
 */
public class MessagingException extends com.zernova.runtime.core.privileged.exception.MessagingException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = 6941498759267936651L;

  public MessagingException(I18nMessage message, CoreEvent event) {
    super(message, event);
  }

  public MessagingException(I18nMessage message, CoreEvent event, Component failingComponent) {
    super(message, event, failingComponent);
  }

  public MessagingException(I18nMessage message, CoreEvent event, Throwable cause) {
    super(message, event, cause);
  }

  public MessagingException(I18nMessage message, CoreEvent event, Throwable cause, Component failingComponent) {
    super(message, event, cause, failingComponent);
  }

  public MessagingException(CoreEvent event, Throwable cause) {
    super(event, cause);
  }

  public MessagingException(CoreEvent event, MessagingException original) {
    super(event, original);
  }

  public MessagingException(CoreEvent event, Throwable cause, Component failingComponent) {
    super(event, cause, failingComponent);
  }
}
