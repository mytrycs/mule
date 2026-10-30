/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.connectivity.oauth;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.ClientCredentials;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * {@link ConnectionProvider} implementations which are also annotated with {@link ClientCredentials} <b>MUST</b> have a field of
 * this type. When the authorization dance is completed, the runtime will inject in such field an instance of this class.
 *
 * This class holds all the relevant information about the completed authorization dance so that the {@link ConnectionProvider}
 * can make use of it.
 *
 * @since 1.2.1
 */
@MinZernovaVersion("4.2.1")
@NoImplement
public interface ClientCredentialsState extends OAuthState {


}
