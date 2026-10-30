/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context.notification;

import com.zernova.runtime.api.notification.AbstractServerNotification;

public class Event1 extends AbstractServerNotification {

  public Event1() {
    super("", NO_ACTION_ID);
  }

  public Event1(String id) {
    super("", NO_ACTION_ID, id);
  }

  @Override
  public String getEventName() {
    return "Event1";
  }
}
