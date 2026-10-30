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

import static com.zernova.runtime.extension.api.runtime.parameter.HttpParameterPlacement.QUERY_PARAMS;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.Expression;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.parameter.HttpParameterPlacement;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Similar to {@link Parameter}, but intended to be used exclusively on fields of a {@link ConnectionProvider} which supports
 * OAuth.
 * <p>
 * The semantics are similar to that of {@link Parameter}, but with some important differences:
 * <p>
 * <ul>
 * <li>When the authorization dance is performed, the values of these parameters are sent as custom parameters to the OAuth
 * provider</li>
 * <li>Unlike traditional parameters (which support expressions by default), these parameters cannot support them. Actually, the
 * {@link Expression} annotation cannot be used alongside this one.</li>
 * <li>Only basic types are supported on OAuth parameters</li>
 * </ul>
 * being sent as a custom OAuth parameter during the authentication dance.
 * <p>
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface OAuthParameter {

  /**
   * The name under which the parameter's value is to be sent to the OAuth provider during the authentication request. Not to be
   * confused with the {@link Alias} annotation, this does not influence the parameter's name on the {@link ExtensionModel}. This
   * parameter can be used alongside {@link Alias} withou any problem.
   * <p>
   * Not specifying this annotation means using the parameter's default name.
   *
   * @return the name of the parameter during the OAuth dance
   */
  String requestAlias() default "";

  /**
   * @return The parameter's placement in the resulting HTTP request
   * @since 1.2.1
   */
  HttpParameterPlacement placement() default QUERY_PARAMS;
}
