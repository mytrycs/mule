/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.connectivity.oauth;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.util.Optional;

/**
 * Provides information about the request that is sent to the authorization url during an OAuth2 dance using the
 * Authorization-Code grant type.
 * <p>
 * If the acting {@link ConnectionProvider} has a {@code before} flow configured, then an instance of this class will be initial
 * payload of such flow.
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
public interface AuthCodeRequest extends com.zernova.sdk.api.connectivity.oauth.AuthCodeRequest {

}
