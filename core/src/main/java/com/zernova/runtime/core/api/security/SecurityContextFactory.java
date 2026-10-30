/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.security;

import com.zernova.runtime.api.security.Authentication;
import com.zernova.runtime.api.security.SecurityContext;

/**
 * <code>SecurityContextFactory</code> is responsible for creating a SecurityContext instance. The factory itself is associated
 * with an Authentication class type on the SecurityManager
 */

public interface SecurityContextFactory {

  SecurityContext create(Authentication authentication);
}
