/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.notification;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.notification.ExtensionNotification;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.util.function.Supplier;

/**
 * This component allows firing {@link ExtensionNotification ExtensionNotifications} with both custom data and default one.
 *
 * @since 1.1
 */
@MinZernovaVersion("4.1")
@NoImplement
public interface NotificationEmitter {

  /**
   * Fires an {@link ExtensionNotification} with the desired information.
   *
   * @param action the {@link NotificationActionDefinition} to use.
   * @param data   the {@link TypedValue} data to use.
   */
  void fire(NotificationActionDefinition action, TypedValue<?> data);

  /**
   * Fires an {@link ExtensionNotification} with the desired information.
   *
   * @param action    the {@link NotificationActionDefinition} to use.
   * @param dataValue a supplier for the the {@link Object} to use as value of the generated {@link DataType}.
   * @param dataType  the type of the data returned by the provided supplier.
   *
   * @since 4.2.0
   */
  default void fireLazy(NotificationActionDefinition action, Supplier<?> dataValue, DataType dataType) {
    fire(action, new TypedValue<>(dataValue.get(), dataType));
  }

}
