/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.message;

import com.zernova.runtime.api.message.AbstractZernovaMessageBuilderFactory;
import com.zernova.runtime.api.message.Message;

/**
 *
 */
public final class DefaultMessageBuilderFactory extends AbstractZernovaMessageBuilderFactory {

  private static DefaultMessageBuilderFactory INSTANCE = new DefaultMessageBuilderFactory();

  public static DefaultMessageBuilderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public InternalMessage.Builder create() {
    return new DefaultMessageBuilder();
  }

  @Override
  public InternalMessage.Builder create(Message message) {
    return new DefaultMessageBuilder(message);
  }

}
