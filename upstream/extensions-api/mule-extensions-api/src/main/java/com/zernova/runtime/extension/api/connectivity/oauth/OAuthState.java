/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.connectivity.oauth;

import com.zernova.api.annotation.NoImplement;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.util.Optional;

/**
 * An object which holds information about an OAuth authorization
 *
 * @since 1.2.1
 */
@MinZernovaVersion("4.2.1")
@NoImplement
public interface OAuthState extends com.zernova.sdk.api.connectivity.oauth.OAuthState {

}
