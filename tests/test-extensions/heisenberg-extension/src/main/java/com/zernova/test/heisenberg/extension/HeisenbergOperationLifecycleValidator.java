/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.ANY;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.extension.api.annotation.param.MediaType;

public class HeisenbergOperationLifecycleValidator implements Initialisable, Startable, Stoppable, Disposable {

  public static final String NOT_INITIALISED = "not_initialised";
  public static int INITIALIZE_CALL_COUNT = 0;
  public static int START_CALL_COUNT = 0;
  public static int STOP_CALL_COUNT = 0;
  public static int DISPOSE_CALL_COUNT = 0;

  private String state = NOT_INITIALISED;

  @MediaType(ANY)
  public void lifecycleValidator(String expected) {
    if (!expected.equals(state)) {
      throw new IllegalStateException(String.format("Expected [%s] but was [%s]", expected, state));
    }
  }

  @Override
  public void initialise() throws InitialisationException {
    checkState(NOT_INITIALISED);
    state = Initialisable.PHASE_NAME;
    INITIALIZE_CALL_COUNT++;
  }

  @Override
  public void start() throws ZernovaException {
    checkState(Initialisable.PHASE_NAME);
    state = Startable.PHASE_NAME;
    START_CALL_COUNT++;
  }

  @Override
  public void stop() throws ZernovaException {
    checkState(Startable.PHASE_NAME);
    state = Stoppable.PHASE_NAME;
    STOP_CALL_COUNT++;
  }

  @Override
  public void dispose() {
    try {
      checkState(Stoppable.PHASE_NAME);
    } catch (InitialisationException e) {
      throw new RuntimeException(e);
    }
    state = Disposable.PHASE_NAME;
    DISPOSE_CALL_COUNT++;
  }

  private void checkState(String expected) throws InitialisationException {
    if (!state.equals(expected)) {
      throw new InitialisationException(
                                        createStaticMessage("Invalid state: expected %s but as %s", expected, state), this);
    }
  }
}
