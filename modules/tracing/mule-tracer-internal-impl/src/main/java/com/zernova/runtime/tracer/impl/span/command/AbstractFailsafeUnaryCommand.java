/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.impl.span.command;

import java.util.function.Function;

import org.slf4j.Logger;

/**
 * A failsafe command with one parameter and a return type.
 *
 * @param <A> the type of the first parameter
 * @param <B> the return type.
 */
public abstract class AbstractFailsafeUnaryCommand<A, B> implements UnaryCommand<A, B> {

  private final FailsafeUnaryCommandExecutor<A, B> failsafeSpanCommand;

  protected AbstractFailsafeUnaryCommand(Logger logger, String errorMessage, boolean propagateExceptions, B onFailureReturn) {
    failsafeSpanCommand = new FailsafeUnaryCommandExecutor<>(logger, errorMessage, propagateExceptions, onFailureReturn);
  }


  @Override
  public B execute(A parameter) {
    return failsafeSpanCommand.execute(getFunction(), parameter);
  }

  abstract Function<A, B> getFunction();
}
