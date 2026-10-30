/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.component;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;

/**
 * The scope of the ZernovaEvent Callback is to be able to get a the message currently being processed by the
 * {@link FunctionalTestProcessor} and make assertions on the message payload, headers or attachments or to make changes required
 * for the test.
 *
 * @see FunctionalTestProcessor
 */
public interface EventCallback {

  void eventReceived(CoreEvent event, Object component, ZernovaContext muleContext) throws Exception;
}
