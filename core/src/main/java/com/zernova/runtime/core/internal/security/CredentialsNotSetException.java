/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.security;

import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.api.security.SecurityContext;
import com.zernova.runtime.core.api.security.SecurityFilter;
import com.zernova.runtime.api.security.UnauthorisedException;

/**
 * <code>CredentialsNotSetException</code> is thrown when user credentials cannot be obtained from the current message
 */
public class CredentialsNotSetException extends UnauthorisedException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = -6271648179641734580L;

  public CredentialsNotSetException(I18nMessage message) {
    super(message);
  }

  public CredentialsNotSetException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

  public CredentialsNotSetException(CoreEvent event, SecurityContext context, SecurityFilter filter) {
    super(context, filter.getClass().getName(),
          event.getContext().getOriginatingLocation().getComponentIdentifier().getIdentifier().getNamespace());
  }
}
