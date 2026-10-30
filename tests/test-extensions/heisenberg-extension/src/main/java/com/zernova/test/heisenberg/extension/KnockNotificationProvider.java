/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static com.zernova.test.heisenberg.extension.HeisenbergNotificationAction.KNOCKED_DOOR;
import static com.zernova.test.heisenberg.extension.HeisenbergNotificationAction.KNOCKING_DOOR;
import com.zernova.runtime.extension.api.annotation.notification.NotificationActionProvider;
import com.zernova.runtime.extension.api.notification.NotificationActionDefinition;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class KnockNotificationProvider implements NotificationActionProvider {

  @Override
  public Set<NotificationActionDefinition> getNotificationActions() {
    return ImmutableSet.<NotificationActionDefinition>builder()
        .add(KNOCKING_DOOR)
        .add(KNOCKED_DOOR)
        .build();
  }

}
