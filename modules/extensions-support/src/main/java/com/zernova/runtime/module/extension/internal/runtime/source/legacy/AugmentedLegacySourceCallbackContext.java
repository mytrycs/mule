/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source.legacy;

import com.zernova.runtime.core.internal.execution.NotificationFunction;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;

import java.util.List;

/**
 * Augments the legacy {@link SourceCallbackContext} contract with internal behavior we don't want exposed on the public API
 *
 * @since 4.4.0
 */
public interface AugmentedLegacySourceCallbackContext extends SourceCallbackContext {

  /**
   * Releases the bound connection
   */
  void releaseConnection();

  /**
   * Indicates that {@code this} instance has already been used to dispatch an event
   */
  void dispatched();

  /**
   * Retrieves the notification functions.
   *
   * @return a list of {@link NotificationFunction NotificationFunctions} to evaluate and fire
   */
  List<NotificationFunction> getNotificationsFunctions();


}
