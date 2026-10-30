/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.execution;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.exception.ZernovaExceptionInfo;
import com.zernova.runtime.api.notification.EnrichedNotificationInfo;

import java.util.Map;

/**
 * Provides a callback to add info entries to an exception just before logging/handling it.
 *
 * When an exception is thrown in a message processor, implementations of this interface will be called in order to augment the
 * exception message with properties that can be helpful to an application developer troubleshooting that exception.
 *
 * @since 3.8.0
 */
@NoImplement
public interface ExceptionContextProvider {

  /**
   * @param notificationInfo
   * @return info entries to be added to the logged exception message
   *
   * @deprecated Use {@link #putContextInfo(ZernovaExceptionInfo, EnrichedNotificationInfo, Component)} instead.
   */
  @Deprecated
  Map<String, Object> getContextInfo(EnrichedNotificationInfo notificationInfo, Component lastProcessed);

  /**
   * @param info             the map to put the entries to be added to the logged exception message into
   * @param notificationInfo
   * @param lastProcessed
   *
   * @since 4.3
   */
  void putContextInfo(ZernovaExceptionInfo info, EnrichedNotificationInfo notificationInfo, Component lastProcessed);

}
