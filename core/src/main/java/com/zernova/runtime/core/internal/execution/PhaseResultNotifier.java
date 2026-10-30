/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.execution;

/**
 * Notifier used by {@link MessageProcessPhase} in order to define the result of the phase execution
 */
public interface PhaseResultNotifier {

  /**
   * This method must be called when the phase complete successfully
   */
  void phaseSuccessfully();

  /**
   * This message must be called when a phase execution throw an exception
   *
   * @param reason exception that represents the failure in the phase
   */
  void phaseFailure(Exception reason);
}
