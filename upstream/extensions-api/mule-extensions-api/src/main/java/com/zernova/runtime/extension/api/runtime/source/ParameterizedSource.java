/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.source;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.extension.api.annotation.execution.OnError;
import com.zernova.runtime.extension.api.annotation.execution.OnSuccess;

import java.util.Map;

/**
 * A contract for obtaining the parameters that were configured into a {@link Source} instance without depending on its actual
 * implementation class.
 *
 * @since 1.0
 */
@NoImplement
public interface ParameterizedSource {

  /**
   * Returns a {@link Map} with the parameters that the source uses for initialising itself. Notice that this will not include any
   * parameters used for the {@link OnSuccess} or {@link OnError} callbacks.
   *
   * @return the source initial parameters.
   */
  Map<String, Object> getInitialisationParameters();
}
