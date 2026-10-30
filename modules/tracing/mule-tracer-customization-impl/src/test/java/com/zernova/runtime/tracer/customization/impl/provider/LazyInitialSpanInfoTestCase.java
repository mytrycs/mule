/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.customization.impl.provider;

import static com.zernova.runtime.core.internal.profiling.NoopCoreEventTracer.getNoopCoreEventTracer;
import static com.zernova.runtime.tracer.api.span.info.InitialExportInfo.NO_EXPORTABLE_DEFAULT_EXPORT_SPAN_CUSTOMIZATION_INFO;
import static com.zernova.test.allure.AllureConstants.Profiling.PROFILING;
import static com.zernova.test.allure.AllureConstants.Profiling.ProfilingServiceStory.TRACING_CUSTOMIZATION;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.tracer.api.context.SpanContext;
import com.zernova.runtime.tracer.api.sniffer.SpanSnifferManager;
import com.zernova.runtime.tracer.api.span.info.InitialExportInfo;
import com.zernova.runtime.tracer.api.span.info.InitialSpanInfo;
import com.zernova.runtime.tracer.impl.CoreEventTracer;
import com.zernova.runtime.tracer.impl.span.InternalSpan;
import com.zernova.runtime.tracer.impl.span.factory.EventSpanFactory;

import org.junit.Test;

import org.mockito.Mockito;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;


@Feature(PROFILING)
@Story(TRACING_CUSTOMIZATION)
public class LazyInitialSpanInfoTestCase {

  public static final String DUMMY_SPAN = "dummy-span";

  @Test
  public void whenNoopCoreEventTracerIsUsedLazyInitializationSpanInfoIsNotComputed() {
    final LazyInitialSpanInfo lazyInitialSpanInfo = new LazyInitialSpanInfo(() -> new InitialSpanInfo() {

      @Override
      public String getName() {
        return DUMMY_SPAN;
      }

      @Override
      public InitialExportInfo getInitialExportInfo() {
        return NO_EXPORTABLE_DEFAULT_EXPORT_SPAN_CUSTOMIZATION_INFO;
      }
    });

    getNoopCoreEventTracer().startSpan(mock(CoreEvent.class), lazyInitialSpanInfo);
    assertThat(lazyInitialSpanInfo.isComputed(), equalTo(false));
  }

  @Test
  public void whenCoreEventTracerIsUsedLazyInitializationSpanInfoIsComputed() throws Exception {
    final LazyInitialSpanInfo lazyInitialSpanInfo = new LazyInitialSpanInfo(() -> new InitialSpanInfo() {

      @Override
      public String getName() {
        return DUMMY_SPAN;
      }

      @Override
      public InitialExportInfo getInitialExportInfo() {
        return NO_EXPORTABLE_DEFAULT_EXPORT_SPAN_CUSTOMIZATION_INFO;
      }
    });

    final CoreEventTracer coreEventTracer = new CoreEventTracer(mock(FeatureFlaggingService.class), new TestEventSpanFactory());
    coreEventTracer.initialise();
    coreEventTracer.startSpan(mock(CoreEvent.class), lazyInitialSpanInfo);
    assertThat(lazyInitialSpanInfo.isComputed(), equalTo(true));
    assertThat(lazyInitialSpanInfo.getName(), equalTo(DUMMY_SPAN));
  }

  private class TestEventSpanFactory implements EventSpanFactory {

    @Override
    public InternalSpan getSpan(SpanContext spanContext,
                                InitialSpanInfo initialSpanInfo) {
      final InternalSpan internalSpan = mock(InternalSpan.class);
      // We retrieve info so that the initialSpanInfo is computed.
      final String name = initialSpanInfo.getName();
      Mockito.when(internalSpan.getName()).thenReturn(name);
      return internalSpan;
    }

    @Override
    public SpanSnifferManager getSpanSnifferManager() {
      return mock(SpanSnifferManager.class);
    }
  }
}
