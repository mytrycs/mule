/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.message;

import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.core.internal.util.collection.TransformingList;

import java.util.List;
import java.util.function.Function;

/**
 * Specialization of {@link TransformingList} which outputs instances of {@link Message}
 *
 * @since 4.4.0
 */
public final class TransformingMessageList extends TransformingList<Message> implements List<Message> {

  public TransformingMessageList(List<Object> delegate, Function<Object, Message> transformer) {
    super(delegate, transformer, Message.class);
  }
}
