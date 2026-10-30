/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.el.validation;

import com.zernova.api.annotation.NoImplement;

/**
 * Represents a constraint violation. Every constraint violation has an unique string which is not typed, a message and a
 * {@link Location}.
 *
 * @since 1.9.0
 */
@NoImplement
public interface ConstraintViolation {

  /**
   * @return The {@link Severity} of the violation. Either {@link Severity#WARNING} or {@link Severity#ERROR}.
   */
  Severity getSeverity();

  /**
   * @return The kind of constraint violation.
   */
  String getKind();

  /**
   * @return A message describing the constraint violated.
   */
  String getMessage();

  /**
   * @return The {@link Location} in the script where the constraint violation happened.
   */
  Location getLocation();

}
