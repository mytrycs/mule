/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension;

import com.zernova.runtime.extension.api.annotation.metadata.TypeResolver;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.sdk.api.annotation.binding.Binding;
import com.zernova.sdk.api.annotation.values.FieldValues;
import com.zernova.test.values.extension.connection.ChatConnection;
import com.zernova.test.values.extension.metadata.JsonTypeResolver;
import com.zernova.test.values.extension.resolver.ChannelsValueProvider;
import com.zernova.test.values.extension.resolver.ChatMultiLevelValueProvider;
import com.zernova.test.values.extension.resolver.WorkspacesValueProvider;

import java.io.InputStream;

public class ChatOperations {

  public void withJsonBodyParameterWithField(@Connection ChatConnection chatConnection, @FieldValues(
      value = ChannelsValueProvider.class,
      targetSelectors = "channelId", bindings = {@Binding(actingParameter = "workspace",
          extractionExpression = "body.workspace")}) @FieldValues(
              value = WorkspacesValueProvider.class,
              targetSelectors = "workspace") @TypeResolver(JsonTypeResolver.class) @Content InputStream body) {}

  public void withJsonBodyParameterWithMultiLevelField(@Connection ChatConnection chatConnection, @FieldValues(
      value = ChatMultiLevelValueProvider.class,
      targetSelectors = {"workspace",
          "channelId"}) @TypeResolver(JsonTypeResolver.class) @com.zernova.sdk.api.annotation.param.Content InputStream body) {}

}
