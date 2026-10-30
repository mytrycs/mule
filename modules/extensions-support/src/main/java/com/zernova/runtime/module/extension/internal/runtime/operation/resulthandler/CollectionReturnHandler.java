/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation.resulthandler;

import static com.zernova.metadata.api.utils.MetadataTypeUtils.isCollection;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.toDataType;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.CollectionDataType;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.util.Preconditions;

import java.util.Collection;

/**
 * {@link ReturnHandler} implementation for collections
 * <p>
 * When creates the {@link Message.Builder} it configures the correct {@link DataType} basing on the original {@link MetadataType}
 *
 * @since 4.1
 */
public final class CollectionReturnHandler implements ReturnHandler<Collection> {

  private final CollectionDataType dataType;

  public CollectionReturnHandler(MetadataType outputType) {
    Preconditions.checkArgument(isCollection(outputType), "The output type is not a collection");
    dataType = (CollectionDataType) toDataType(outputType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Message.Builder toMessageBuilder(Collection value) {
    return Message.builder().payload(new TypedValue<>(value, getDataType()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DataType getDataType() {
    return dataType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean handles(Object value) {
    return value instanceof Collection;
  }
}
