/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.client.operation;

import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.extension.api.client.OperationParameterizer;

import java.util.Optional;

/**
 * Expands the {@link OperationParameterizer} contract with internal behavior
 *
 * @since 4.5.0
 */
public interface InternalOperationParameterizer extends OperationParameterizer {

  /**
   * @return an {@link Optional} contextual {@link CoreEvent}
   */
  Optional<CoreEvent> getContextEvent();

}
