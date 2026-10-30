/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.api.context;

/**
 * A component that carries information about the distributed trace context
 *
 * @since 4.5.0
 */
public interface SpanContextAware {

  /**
   * @return the {@link SpanContext} associated with the event.
   *
   * @since 4.5.0
   */
  SpanContext getSpanContext();

  /**
   * @param spanContext the distributed trace context to set.
   *
   * @since 4.5.0
   */
  void setSpanContext(SpanContext spanContext);
}
