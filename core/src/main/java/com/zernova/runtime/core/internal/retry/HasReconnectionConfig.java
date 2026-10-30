/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.retry;

import com.zernova.runtime.core.api.retry.ReconnectionConfig;

import java.util.Optional;

/**
 * Contract interface for a component which may have a {@link ReconnectionConfig}
 *
 * @since 1.0
 */
public interface HasReconnectionConfig {

  Optional<ReconnectionConfig> getReconnectionConfig();
}
