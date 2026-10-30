/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.connectivity.oauth;

import static com.zernova.runtime.extension.api.runtime.parameter.HttpParameterPlacement.QUERY_PARAMS;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.OAuthParameter;
import com.zernova.runtime.extension.api.runtime.parameter.HttpParameterPlacement;

/**
 * A private {@link ModelProperty} used on {@link ParameterModel} instances, indicating that such parameter should be used as an
 * OAuth parameter.
 * <p>
 * This is equivalent to the {@link OAuthParameter} annotation
 *
 * @since 1.0
 */
public class OAuthParameterModelProperty implements ModelProperty {

  private final String requestAlias;
  private final HttpParameterPlacement placement;

  /**
   * Creates a new instance using {@link HttpParameterPlacement#QUERY_PARAMS} as the default placement.
   *
   * @param requestAlias the name under which the parameter is sent to the OAuth provider during the authentication dance
   * @deprecated since 1.2.1. Use {@link #OAuthParameterModelProperty(String, HttpParameterPlacement)} instead
   */
  @Deprecated
  public OAuthParameterModelProperty(String requestAlias) {
    this(requestAlias, QUERY_PARAMS);
  }

  public OAuthParameterModelProperty(String requestAlias, HttpParameterPlacement placement) {
    this.requestAlias = requestAlias;
    this.placement = placement;
  }

  /**
   * @return the name under which the parameter is sent to the OAuth provider during the authentication dance
   */
  public String getRequestAlias() {
    return requestAlias;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code oauthParameter}
   */
  @Override
  public String getName() {
    return "oauthParameter";
  }

  /**
   * @return The parameter's placement
   * @since 1.2.1
   */
  public HttpParameterPlacement getPlacement() {
    return placement;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}
   */
  @Override
  public boolean isPublic() {
    return false;
  }
}
