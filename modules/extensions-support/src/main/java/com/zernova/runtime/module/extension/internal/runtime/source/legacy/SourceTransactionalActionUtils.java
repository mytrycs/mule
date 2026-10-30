/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source.legacy;

import static java.lang.String.format;

import com.zernova.sdk.api.tx.SourceTransactionalAction;

/**
 * Utils class for handling {@link SourceTransactionalAction}
 *
 * @since 4.5.0
 */
public final class SourceTransactionalActionUtils {

  private SourceTransactionalActionUtils() {}

  /**
   * Returns the assosiated {@link SourceTransactionalAction} from the given value. The given value must either be an
   * {@link SourceTransactionalAction} or an {@link com.zernova.sdk.api.tx.SourceTransactionalAction}
   *
   * @param sourceTransactionalAction the value to take the {@link SourceTransactionalAction}
   * @return the {@link SourceTransactionalAction} associated to the given argument.
   */
  public static SourceTransactionalAction from(Object sourceTransactionalAction) {
    if (sourceTransactionalAction instanceof SourceTransactionalAction) {
      return (SourceTransactionalAction) sourceTransactionalAction;
    } else if (sourceTransactionalAction instanceof com.zernova.runtime.extension.api.tx.SourceTransactionalAction) {
      return fromLegacy((com.zernova.runtime.extension.api.tx.SourceTransactionalAction) sourceTransactionalAction);
    }
    throw new IllegalArgumentException(format("SourceTransactionalAction is expected to be a com.zernova.sdk.api.tx.SourceTransactionalAction or com.zernova.runtime.extension.api.tx.SourceTransactionalAction, but was %s",
                                              sourceTransactionalAction.getClass()));
  }

  /**
   * Returns the assosiated {@link SourceTransactionalAction} from the given value. The given value must either be an
   * {@link SourceTransactionalAction} or an {@link com.zernova.sdk.api.tx.SourceTransactionalAction}
   *
   * @param sourceTransactionalAction the value to take the {@link SourceTransactionalAction}
   * @return the {@link SourceTransactionalAction} associated to the given argument.
   */
  public static com.zernova.runtime.extension.api.tx.SourceTransactionalAction toLegacy(Object sourceTransactionalAction) {
    if (sourceTransactionalAction instanceof com.zernova.runtime.extension.api.tx.SourceTransactionalAction) {
      return (com.zernova.runtime.extension.api.tx.SourceTransactionalAction) sourceTransactionalAction;
    } else if (sourceTransactionalAction instanceof SourceTransactionalAction) {
      return toLegacy((SourceTransactionalAction) sourceTransactionalAction);
    }
    throw new IllegalArgumentException(format("SourceTransactionalAction is expected to be a com.zernova.sdk.api.tx.SourceTransactionalAction or com.zernova.runtime.extension.api.tx.SourceTransactionalAction, but was %s",
                                              sourceTransactionalAction.getClass()));
  }

  private static com.zernova.runtime.extension.api.tx.SourceTransactionalAction toLegacy(SourceTransactionalAction sourceTransactionalAction) {
    switch (sourceTransactionalAction) {
      case ALWAYS_BEGIN:
        return com.zernova.runtime.extension.api.tx.SourceTransactionalAction.ALWAYS_BEGIN;
      case NONE:
        return com.zernova.runtime.extension.api.tx.SourceTransactionalAction.NONE;
    }
    return null;
  }

  private static SourceTransactionalAction fromLegacy(com.zernova.runtime.extension.api.tx.SourceTransactionalAction sourceTransactionalAction) {
    switch (sourceTransactionalAction) {
      case ALWAYS_BEGIN:
        return com.zernova.sdk.api.tx.SourceTransactionalAction.ALWAYS_BEGIN;
      case NONE:
        return com.zernova.sdk.api.tx.SourceTransactionalAction.NONE;
    }
    return null;
  }
}
