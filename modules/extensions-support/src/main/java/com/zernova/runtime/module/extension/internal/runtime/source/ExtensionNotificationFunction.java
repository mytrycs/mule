/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import com.zernova.runtime.core.internal.execution.NotificationFunction;

/**
 * An extension specific {@link NotificationFunction} which provides access to the action name that will be fired to allow
 * validations.
 *
 * @since 4.1
 */
public interface ExtensionNotificationFunction extends NotificationFunction {

  /**
   * @return the action that the notification will fire
   */
  String getActionName();

}
