/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.api.component.ComponentIdentifier.builder;

import com.zernova.runtime.api.component.ComponentIdentifier;

public class OperationDoesNotHaveApikitRouter extends ZernovaSdkOperationDoesNotHaveForbiddenComponents {

  private static final ComponentIdentifier APIKIT_ROUTER_IDENTIFIER =
      builder().namespace("apikit").name("router").build();

  @Override
  protected ComponentIdentifier forbiddenComponentIdentifier() {
    return APIKIT_ROUTER_IDENTIFIER;
  }
}
