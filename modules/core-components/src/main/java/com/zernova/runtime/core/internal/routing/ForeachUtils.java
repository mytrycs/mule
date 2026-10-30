/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import static com.zernova.runtime.core.api.util.StreamingUtils.updateTypedValueForStreaming;

import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.internal.routing.outbound.EventBuilderConfigurer;

class ForeachUtils {

  private ForeachUtils() {
    // nothing to do
  }

  static TypedValue manageTypedValueForStreaming(TypedValue typedValue, CoreEvent event, StreamingManager streamingManager) {
    if (typedValue.getValue() instanceof EventBuilderConfigurer) {
      return typedValue;
    } else if (typedValue.getValue() instanceof Message) {
      return updateTypedValueForStreaming(((Message) typedValue.getValue()).getPayload(),
                                          event, streamingManager);
    } else {
      return updateTypedValueForStreaming(typedValue, event, streamingManager);
    }
  }

}
