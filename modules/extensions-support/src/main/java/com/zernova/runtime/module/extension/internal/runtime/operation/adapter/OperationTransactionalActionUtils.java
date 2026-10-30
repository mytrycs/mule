/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation.adapter;

import static java.lang.String.format;

import com.zernova.sdk.api.tx.OperationTransactionalAction;

/**
 * Utils class for handling {@link OperationTransactionalAction}
 *
 * @since 4.5.0
 */
public final class OperationTransactionalActionUtils {

  private OperationTransactionalActionUtils() {}

  /**
   * Returns the assosiated {@link OperationTransactionalAction} from the given value. The given value must either be an
   * {@link OperationTransactionalAction} or an {@link com.zernova.sdk.api.tx.OperationTransactionalAction}
   *
   * @param operationTransactionalAction the value to take the {@link OperationTransactionalAction}
   * @return the {@link OperationTransactionalAction} associated to the given argument.
   */
  public static OperationTransactionalAction from(Object operationTransactionalAction) {
    if (operationTransactionalAction instanceof OperationTransactionalAction) {
      return (OperationTransactionalAction) operationTransactionalAction;
    } else if (operationTransactionalAction instanceof com.zernova.sdk.api.tx.OperationTransactionalAction) {
      return fromSdk((com.zernova.sdk.api.tx.OperationTransactionalAction) operationTransactionalAction);
    }
    throw new IllegalArgumentException(format("operationTransactionalAction is expected to be a com.zernova.sdk.api.tx.OperationTransactionalAction or com.zernova.runtime.extension.api.tx.OperationTransactionalAction, but was %s",
                                              operationTransactionalAction.getClass()));
  }

  private static OperationTransactionalAction fromSdk(com.zernova.sdk.api.tx.OperationTransactionalAction operationTransactionalAction) {
    switch (operationTransactionalAction) {
      case JOIN_IF_POSSIBLE:
        return OperationTransactionalAction.JOIN_IF_POSSIBLE;
      case NOT_SUPPORTED:
        return OperationTransactionalAction.NOT_SUPPORTED;
      case ALWAYS_JOIN:
        return OperationTransactionalAction.ALWAYS_JOIN;
    }
    return null;
  }
}
