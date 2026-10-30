/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.policy;

import com.zernova.runtime.deployment.model.api.policy.PolicyTemplate;

/**
 * Thrown to indicate that an error was found while attempting to create a {@link PolicyTemplate} artifact.
 */
public class PolicyTemplateCreationException extends RuntimeException {

  /**
   * {@inheritDoc}
   */
  public PolicyTemplateCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
