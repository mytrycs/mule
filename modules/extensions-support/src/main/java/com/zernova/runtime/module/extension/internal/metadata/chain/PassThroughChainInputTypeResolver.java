/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata.chain;

import com.zernova.metadata.message.api.MessageMetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.sdk.api.metadata.ChainInputMetadataContext;
import com.zernova.sdk.api.metadata.resolving.ChainInputTypeResolver;

/**
 * {@link ChainInputTypeResolver} implementation that simply returns the received
 * {@link ChainInputMetadataContext#getInputMessageMetadataType()}
 *
 * @since 4.7.0
 */
public class PassThroughChainInputTypeResolver implements ChainInputTypeResolver {

  public static PassThroughChainInputTypeResolver INSTANCE = new PassThroughChainInputTypeResolver();

  private PassThroughChainInputTypeResolver() {}

  @Override
  public MessageMetadataType getChainInputMetadataType(ChainInputMetadataContext context)
      throws MetadataResolvingException, ConnectionException {
    return context.getInputMessageMetadataType();
  }

  @Override
  public String getCategoryName() {
    return "SCOPE_PASSTHROUGH";
  }

  @Override
  public String getResolverName() {
    return "SCOPE_INPUT_PASSTHROUGH";
  }
}
