/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.security;

import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.security.SecurityContext;
import com.zernova.runtime.api.security.SecurityException;
import com.zernova.runtime.api.security.SecurityProviderNotFoundException;
import com.zernova.runtime.api.security.UnknownAuthenticationTypeException;
import com.zernova.runtime.core.api.event.CoreEvent;

/**
 * <code>AuthenticationFilter</code> is a base filter for authenticating messages.
 *
 * @deprecated Mule Runtime no longer provides implementations for this.
 */
@Deprecated
public interface AuthenticationFilter extends SecurityFilter {

  SecurityContext authenticate(CoreEvent event)
      throws SecurityException, UnknownAuthenticationTypeException, CryptoFailureException,
      SecurityProviderNotFoundException, EncryptionStrategyNotFoundException, InitialisationException;
}
