/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.processor;

import static com.zernova.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.TypedException;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.core.api.event.CoreEvent;

/**
 * Processor capable of raising errors on demand, given a type and optionally a message.
 *
 * @since 4.0
 */
public final class RaiseErrorProcessor extends AbstractRaiseErrorProcessor {

  @Override
  protected ComponentIdentifier calculateErrorIdentifier(String typeId) {
    return buildFromStringRepresentation(typeId);
  }

  @Override
  protected TypedException getException(ErrorType type, String message, CoreEvent event) {
    return new TypedException(new DefaultZernovaException(message), type);
  }
}
