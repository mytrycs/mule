/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.execution;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.notification.Notification;

import java.util.function.BiFunction;

/**
 * A {@link java.util.function.BiFunction} that generates {@link Notification Notifications} from a given {@link Event} and a
 * given {@link Component}.
 *
 * @since 4.1
 */
@FunctionalInterface
public interface NotificationFunction extends BiFunction<Event, Component, Notification> {

}
