/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.parameter;

import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Describes the possible placements a parameter can have inside a HTTP request
 *
 * @since 1.2.1
 */
@MinZernovaVersion("4.2.1")
public enum HttpParameterPlacement {

  /**
   * The parameter will be sent as a query parameter
   */
  @MinZernovaVersion("4.2.1")
  QUERY_PARAMS,

  /**
   * The parameter will be sent as a header
   */
  @MinZernovaVersion("4.2.1")
  HEADERS,

  /**
   * The parameter will be added as part of an {@code application/x-www-form-urlencoded} Http request body
   */
  @MinZernovaVersion("4.5.0")
  BODY
}
