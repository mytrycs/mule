/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.profiling.NoopCoreEventTracer;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.extension.api.runtime.parameter.CorrelationInfo;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.client.NullComponent;
import com.zernova.runtime.module.extension.internal.runtime.parameter.ImmutableCorrelationInfo;

import static com.zernova.runtime.core.internal.profiling.NoopCoreEventTracer.getNoopCoreEventTracer;
import static com.zernova.runtime.module.extension.internal.runtime.client.NullComponent.NULL_COMPONENT;

/**
 * {@link ArgumentResolver} that yields instances of {@link CorrelationInfo}
 *
 * @since 4.1
 */
public class CorrelationInfoArgumentResolver implements ArgumentResolver<CorrelationInfo> {

  @Override
  public CorrelationInfo resolve(ExecutionContext executionContext) {
    CoreEvent event = ((ExecutionContextAdapter) executionContext).getEvent();
    if (((ExecutionContextAdapter<?>) executionContext).getComponent().equals(NULL_COMPONENT)) {
      // TODO: W-13837896
      // If this was executed from a null component don't allow to execute any tracing
      // from the connector. This is especially used in the cases where an extension in an
      // operation invokes another extension through an ExtensionsClient.
      return new ImmutableCorrelationInfo(event.getContext().getId(), true, event.getCorrelationId(),
                                          event.getItemSequenceInfo().orElse(null), event, getNoopCoreEventTracer());
    }
    return new ImmutableCorrelationInfo(event.getContext().getId(), true, event.getCorrelationId(),
                                        event.getItemSequenceInfo().orElse(null), event);
  }
}
