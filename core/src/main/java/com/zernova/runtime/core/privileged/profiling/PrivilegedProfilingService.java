/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.profiling;

import static java.util.Collections.emptySet;

import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.profiling.ProfilingDataConsumer;
import com.zernova.runtime.api.profiling.ProfilingEventContext;
import com.zernova.runtime.api.profiling.ProfilingService;
import com.zernova.runtime.tracer.api.context.getter.DistributedTraceContextGetter;
import com.zernova.runtime.tracer.api.sniffer.CapturedExportedSpan;
import com.zernova.runtime.tracer.api.sniffer.ExportedSpanSniffer;
import com.zernova.runtime.tracer.api.sniffer.SpanSnifferManager;

import java.util.Collection;

/**
 * A {@link ProfilingService} that allows to perform some extra privileged operations.
 */
public interface PrivilegedProfilingService extends ProfilingService {

  /**
   * Registers a {@link ProfilingDataConsumer} dynamically.
   *
   * @param profilingDataConsumer the {@link ProfilingDataConsumer} to register.
   * @param <T>                   the {@link ProfilingEventContext} corresponding to the profiling event types the data consumer
   *                              listens to.
   */
  <T extends ProfilingEventContext> void registerProfilingDataConsumer(ProfilingDataConsumer<T> profilingDataConsumer);

  void injectDistributedTraceContext(EventContext eventContext,
                                     DistributedTraceContextGetter distributedTraceContextGetter);

  /**
   * @return gets an {@link SpanSnifferManager}.
   *
   *         This is used for capturing spans in privileged modules but should not be exposed as API.
   *
   * @since 4.5.0
   */
  default SpanSnifferManager getSpanExportManager() {
    return new SpanSnifferManager() {

      @Override
      public ExportedSpanSniffer getExportedSpanSniffer() {
        return new ExportedSpanSniffer() {

          @Override
          public Collection<CapturedExportedSpan> getExportedSpans() {
            return emptySet();
          }

          @Override
          public void dispose() {
            // Nothing to dispose.
          }
        };
      }
    };
  }
}
