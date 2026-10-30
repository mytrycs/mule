/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.notification;

import com.zernova.runtime.extension.api.notification.NotificationActionDefinition;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.util.Set;

/**
 * A component responsible for providing an immutable {@link Set} of {@link NotificationActionDefinition}s.
 *
 * @since 1.1
 * @see Fires
 * @see NotificationActionDefinition
 */
@MinZernovaVersion("4.1")
public interface NotificationActionProvider {

  /**
   * @return a {@link Set} of {@link NotificationActionDefinition}.
   */
  Set<NotificationActionDefinition> getNotificationActions();

}
