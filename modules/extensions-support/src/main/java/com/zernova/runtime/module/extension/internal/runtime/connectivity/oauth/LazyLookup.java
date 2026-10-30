/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;

/**
 * {@link LazyValue} specialization which lookups and object of a certain type in the mule registry
 *
 * @param <T> the generic type of the object to look for
 * @since 4.0
 */
public class LazyLookup<T> extends LazyValue<T> {

  public LazyLookup(Class<T> type, ZernovaContext muleContext) {
    super(() -> {
      try {
        return ((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(type);
      } catch (Exception e) {
        throw new ZernovaRuntimeException(createStaticMessage("Could not fetch dependency of type " + type.getName()), e);
      }
    });
  }
}
