/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.resolver;

import java.util.Set;

import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.sdk.api.values.Value;
import com.zernova.sdk.api.values.ValueBuilder;
import com.zernova.sdk.api.values.ValueProvider;
import com.zernova.test.values.extension.connection.ChatConnection;

public class WorkspacesValueProvider implements ValueProvider {

  @Connection
  private ChatConnection chatConnection;

  @Override
  public Set<Value> resolve() {
    return ValueBuilder.getValuesFor(chatConnection.getWorkspaces());
  }

  @Override
  public String getId() {
    return "Workspaces value provider";
  }
}
