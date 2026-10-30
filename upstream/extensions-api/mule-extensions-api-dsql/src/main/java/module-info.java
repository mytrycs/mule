/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * API for Mule Extensions to add DateSense Query Language support.
 *
 * @moduleGraph
 * @since 1.5
 */
module com.zernova.runtime.extensions.api.dsql {

  requires com.zernova.runtime.extensions.api;

  requires antlr.runtime;

  provides com.zernova.runtime.extension.api.dsql.DsqlParser
      with com.zernova.runtime.extension.internal.dsql.DefaultDsqlParser;

}
