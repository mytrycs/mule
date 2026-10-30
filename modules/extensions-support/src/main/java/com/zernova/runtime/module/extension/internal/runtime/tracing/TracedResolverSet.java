/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.tracing;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.tracer.api.component.ComponentTracer;

import java.util.Map;

/**
 * An extended {@link ResolverSet} that will trace the resolution of its composed {@link ValueResolver}s.
 *
 * @since 4.5
 */
public class TracedResolverSet extends ResolverSet {

  private final ComponentTracer<CoreEvent> valueResolutionTracer;

  public TracedResolverSet(ZernovaContext muleContext,
                           ComponentTracer<CoreEvent> valueResolutionTracer) {
    super(muleContext);
    this.valueResolutionTracer = valueResolutionTracer;
  }

  @Override
  protected Object resolve(Map.Entry<String, ValueResolver<?>> entry, ValueResolvingContext valueResolvingContext)
      throws ZernovaException {
    valueResolutionTracer.startSpan(valueResolvingContext.getEvent());
    try {
      valueResolutionTracer.addCurrentSpanAttribute(valueResolvingContext.getEvent(), "value-name", entry.getKey());
      return super.resolve(entry, valueResolvingContext);
    } finally {
      valueResolutionTracer.endCurrentSpan(valueResolvingContext.getEvent());
    }
  }
}
