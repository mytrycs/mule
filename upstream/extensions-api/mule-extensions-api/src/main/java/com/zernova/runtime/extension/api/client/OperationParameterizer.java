/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.client;

import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.extension.api.client.params.ComponentParameterizer;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Parameterizes an operation execution performed through the {@link ExtensionsClient}
 *
 * @since 1.5.0
 */
@MinZernovaVersion("4.5.0")
public interface OperationParameterizer extends ComponentParameterizer<OperationParameterizer> {

  /**
   * Specifies an event to which this operation is relative to. This means that parameters assigned with expression values, or
   * managed streams returned will be associated to this event. If not specified, a temporary event will be generated for the
   * execution and discarded immediately after.
   *
   * @param event an {@link Event}
   * @return {@code this} instance
   */
  OperationParameterizer inTheContextOf(Event event);

}
