/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.core.context.notification;

import com.zernova.runtime.api.notification.Notification;

import java.util.List;

/**
 * Provides access to the notifications generated during a test run.
 *
 * @param <T> the type of notifications to store.
 * @since 4.0
 */
public interface NotificationLogger<T extends Notification> {

  /**
   * @return the notifications generated during a test run.
   */
  public List<T> getNotifications();

}
