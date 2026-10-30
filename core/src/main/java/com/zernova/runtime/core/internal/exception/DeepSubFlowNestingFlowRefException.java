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
 * Exception thrown when a too many nested static subFlows are encountered.
 *
 * @since 4.4, 4.3.1
 */
public class DeepSubFlowNestingFlowRefException extends LifecycleException {

  private static final long serialVersionUID = 2988543857363646772L;

  public DeepSubFlowNestingFlowRefException(String offendingFlowName, Processor flowRefProcessor) {
    super(createStaticMessage("Found too many nested static 'sub-flow's: " + offendingFlowName),
          flowRefProcessor);

  }
}
