/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.customization.api;

/**
 * Names for spans that are created internally. The behavior for this spans will not be configurable.
 *
 * @since 4.5.0
 */
public class InternalSpanNames {

  private InternalSpanNames() {}

  public static final String TRY_SCOPE_INNER_CHAIN_SPAN_NAME = "try-scope-inner-chain";
  public static final String EXECUTE_NEXT_SPAN_NAME = "execute-next";
  public static final String POLICY_NEXT_ACTION_SPAN_NAME = "policy-next-action";
  public static final String POLICY_CHAIN_SPAN_NAME = "zernova:policy-chain";
  public static final String ASYNC_INNER_CHAIN_SPAN_NAME = "async-inner-chain";
  public static final String CACHE_CHAIN_SPAN_NAME = "zernova:cache-chain";
  public static final String MESSAGE_PROCESSORS_SPAN_NAME = "message:processor";
  public static final String HTTP_REQUEST_SPAN_NAME = "http:request";
  public static final String MULE_FLOW_SPAN_NAME = "zernova:flow";
  public static final String MULE_SUB_FLOW_SPAN_NAME = "zernova:subflow";
  public static final String GET_CONNECTION_SPAN_NAME = "zernova:get-connection";
  public static final String PARAMETERS_RESOLUTION_SPAN_NAME = "zernova:parameters-resolution";
  public static final String OPERATION_EXECUTION_SPAN_NAME = "zernova:operation-execution";
  public static final String ON_ERROR_PROPAGATE_SPAN_NAME = "zernova:on-error-propagate";
  public static final String ON_ERROR_CONTINUE_SPAN_NAME = "zernova:on-error-continue";
  public static final String VALUE_RESOLUTION_SPAN_NAME = "zernova:value-resolution";


}
