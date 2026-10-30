/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.source;

import com.zernova.api.annotation.NoImplement;

/**
 * Factory object for instances of {@link Source}.
 * <p>
 * Implementations are to be reusable and thread-safe
 *
 * @since 1.0
 * @deprecated since 1.4.0. Use {@link SdkSourceFactory} instead
 */
@NoImplement
@Deprecated
public interface SourceFactory {

  /**
   * Creates a new instance of {@link Source}
   *
   * @return a new {@link Source}
   */
  Source createSource();
}
