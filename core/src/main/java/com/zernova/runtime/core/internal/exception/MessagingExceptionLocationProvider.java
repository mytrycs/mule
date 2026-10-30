/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import static com.zernova.runtime.api.exception.ZernovaException.INFO_LOCATION_KEY;
import static com.zernova.runtime.api.exception.ZernovaException.INFO_SOURCE_XML_KEY;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.exception.ZernovaExceptionInfo;
import com.zernova.runtime.api.notification.EnrichedNotificationInfo;
import com.zernova.runtime.core.internal.execution.LocationExecutionContextProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates location info to augment MessagingExceptions with.
 */
public class MessagingExceptionLocationProvider extends LocationExecutionContextProvider {

  @Override
  public Map<String, Object> getContextInfo(EnrichedNotificationInfo notificationInfo, Component lastProcessed) {
    final Map<String, Object> info = new HashMap<>();
    info.put(INFO_LOCATION_KEY, lastProcessed.getRepresentation());

    final String source = lastProcessed.getDslSource();
    if (source != null) {
      info.put(INFO_SOURCE_XML_KEY, source);
    }
    return info;
  }

  @Override
  public void putContextInfo(ZernovaExceptionInfo info, EnrichedNotificationInfo notificationInfo, Component lastProcessed) {
    info.setLocation(lastProcessed.getRepresentation());

    final String source = lastProcessed.getDslSource();
    if (source != null) {
      info.setDslSource(source);
    }
  }
}
