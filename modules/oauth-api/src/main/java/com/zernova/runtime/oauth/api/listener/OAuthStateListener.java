/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.oauth.api.listener;

/**
 * Provides compatibility with version 1.x of the mule-oauth-client, which is a transitive api of the service api.
 *
 * @deprecated since 1.5, use {@link com.zernova.oauth.client.api.listener.OAuthStateListener} from {@code mule-oauth-client 2.x}.
 */
@Deprecated
public interface OAuthStateListener extends com.zernova.oauth.client.api.listener.OAuthStateListener {

}
