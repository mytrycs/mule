/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.interceptor;

import com.zernova.runtime.api.interception.SourceInterceptor;
import com.zernova.runtime.api.interception.SourceInterceptorFactory;
import com.zernova.runtime.core.api.source.MessageSource;
import com.zernova.runtime.core.internal.event.InternalEvent;
import com.zernova.runtime.core.internal.policy.SourcePolicySuccessResult;

/**
 * Hooks the {@link SourceInterceptor}s for a {@link MessageSource} successful callback into the {@code Reactor} response handling
 * pipeline.
 *
 * @since 4.3.0
 */
public class CompletableInterceptorSourceSuccessCallbackAdapter
    extends AbstractCompletableInterceptorSourceCallbackAdapter<SourcePolicySuccessResult> {


  public CompletableInterceptorSourceSuccessCallbackAdapter(SourceInterceptorFactory interceptorFactory) {
    super(interceptorFactory);
  }

  @Override
  protected SourcePolicySuccessResult applyBefore(InternalEvent event, SourcePolicySuccessResult result) {
    return new SourcePolicySuccessResult(event, result.getResponseParameters(),
                                         result.getMessageSourceResponseParametersProcessor());
  }

}
