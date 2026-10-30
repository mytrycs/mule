/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.connectivity.oauth;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used to annotate fields on a {@link ConnectionProvider} which supports OAuth.
 * <p>
 * The annotation is used to provide and expression which is evaluated against the response of the access token url. The result of
 * evaluating such expression will be injected into the annotated field.
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface OAuthCallbackValue {

  /**
   * Expression to extract the parameter from the oauth response
   */
  String expression();
}
