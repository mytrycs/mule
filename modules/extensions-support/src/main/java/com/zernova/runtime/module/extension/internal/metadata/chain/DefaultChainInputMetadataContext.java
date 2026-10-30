/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata.chain;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.message.api.MessageMetadataType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.descriptor.InputMetadataDescriptor;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.module.extension.api.metadata.PropagatedParameterTypeResolver;
import com.zernova.runtime.module.extension.internal.metadata.SdkMetadataContextAdapter;
import com.zernova.sdk.api.metadata.ChainInputMetadataContext;
import com.zernova.sdk.api.metadata.MetadataCache;
import com.zernova.sdk.api.metadata.MetadataContext;
import com.zernova.sdk.api.metadata.RouterOutputMetadataContext;
import com.zernova.sdk.api.metadata.ScopeOutputMetadataContext;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Default implementation of {@link ChainInputMetadataContext}
 *
 * @since 4.7.0
 */
public class DefaultChainInputMetadataContext implements ChainInputMetadataContext {

  private final Supplier<MessageMetadataType> inputMessageMetadataType;
  private final InputMetadataDescriptor inputMetadataDescriptor;
  private final MetadataContext rootContext;
  private final PropagatedParameterTypeResolver propagatedParameterTypeResolver;

  public DefaultChainInputMetadataContext(Supplier<MessageMetadataType> inputMessageMetadataType,
                                          InputMetadataDescriptor inputMetadataDescriptor,
                                          PropagatedParameterTypeResolver propagatedParameterTypeResolver,
                                          MetadataContext rootContext) {
    this.inputMessageMetadataType = new LazyValue<>(inputMessageMetadataType);
    this.inputMetadataDescriptor = inputMetadataDescriptor;
    this.propagatedParameterTypeResolver = propagatedParameterTypeResolver;
    this.rootContext = rootContext;
  }

  public DefaultChainInputMetadataContext(Supplier<MessageMetadataType> inputMessageMetadataType,
                                          InputMetadataDescriptor inputMetadataDescriptor,
                                          PropagatedParameterTypeResolver propagatedParameterTypeResolver,
                                          com.zernova.runtime.api.metadata.MetadataContext rootContext) {
    this(inputMessageMetadataType, inputMetadataDescriptor, propagatedParameterTypeResolver,
         new SdkMetadataContextAdapter(rootContext));
  }

  @Override
  public MetadataType getParameterResolvedType(String parameterName) throws NoSuchElementException, MetadataResolvingException {
    try {
      return propagatedParameterTypeResolver.getResolvedType(parameterName)
          .orElseGet(() -> inputMetadataDescriptor.getParameterMetadata(parameterName).getType());
    } catch (IllegalArgumentException e) {
      throw new NoSuchElementException(e.getMessage());
    }
  }

  @Override
  public MessageMetadataType getInputMessageMetadataType() {
    return inputMessageMetadataType.get();
  }

  @Override
  public void dispose() {
    rootContext.dispose();
  }

  @Override
  public <C> Optional<C> getConnection() throws ConnectionException {
    return rootContext.getConnection();
  }

  @Override
  public ClassTypeLoader getTypeLoader() {
    return rootContext.getTypeLoader();
  }

  @Override
  public BaseTypeBuilder getTypeBuilder() {
    return rootContext.getTypeBuilder();
  }

  @Override
  public MetadataCache getCache() {
    return rootContext.getCache();
  }

  @Override
  public Optional<RouterOutputMetadataContext> getRouterOutputMetadataContext() {
    return rootContext.getRouterOutputMetadataContext();
  }

  @Override
  public Optional<ScopeOutputMetadataContext> getScopeOutputMetadataContext() {
    return rootContext.getScopeOutputMetadataContext();
  }
}
