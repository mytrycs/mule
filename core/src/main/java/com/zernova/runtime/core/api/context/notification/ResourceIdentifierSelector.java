/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context.notification;

import com.zernova.runtime.api.notification.AbstractServerNotification;
import com.zernova.runtime.api.notification.Notification;

import java.util.function.Predicate;

/**
 * Notification listener selector that matches a notification form a component with a given identifier.
 *
 * @since 4.5
 */
public final class ResourceIdentifierSelector implements Predicate<Notification> {

  private final String subscription;

  public ResourceIdentifierSelector(String subscription) {
    this.subscription = subscription;
  }

  @Override
  public boolean test(Notification notification) {
    return subscription != null ? subscription
        .equals(((AbstractServerNotification) notification).getResourceIdentifier()) : true;
  }

  @Override
  public String toString() {
    return "selector(" + subscription + ")";
  }
}
