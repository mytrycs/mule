/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.span.error;

import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.core.api.context.notification.FlowCallStack;
import com.zernova.runtime.tracer.api.span.error.InternalSpanError;
import com.zernova.runtime.tracer.api.span.InternalSpanCallStack;

public class DefaultSpanError implements InternalSpanError {

  private final Error error;
  private final InternalSpanCallStack errorStackTrace;
  private final boolean isEscapingSpan;

  public DefaultSpanError(Error error, InternalSpanCallStack errorStackTrace, boolean isEscapingSpan) {
    this.error = error;
    this.errorStackTrace = errorStackTrace;
    this.isEscapingSpan = isEscapingSpan;
  }

  @Override
  public Error getError() {
    return error;
  }

  @Override
  public boolean isEscapingSpan() {
    return isEscapingSpan;
  }

  @Override
  public InternalSpanCallStack getErrorStacktrace() {
    return errorStackTrace;
  }

}
