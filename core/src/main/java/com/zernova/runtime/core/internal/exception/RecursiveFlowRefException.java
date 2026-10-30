/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.lifecycle.LifecycleException;
import com.zernova.runtime.core.api.processor.Processor;

/**
 * Exception thrown when a recursion exists between static flow-refs.
 *
 * @since 4.3.0
 */
public class RecursiveFlowRefException extends LifecycleException {

  private static final long serialVersionUID = 336572098969292323L;

  public RecursiveFlowRefException(String offendingFlowName, Processor flowRefProcessor) {
    super(createStaticMessage("Found a possible infinite recursion involving flows named " + offendingFlowName),
          flowRefProcessor);

  }
}
