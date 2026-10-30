/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.config;

import com.zernova.api.annotation.NoImplement;

/**
 * Marker interface for custom configuration extensions defined inside {@link ZernovaConfiguration}
 *
 * This is required by the new parsers to identify child elements
 *
 * @since 4.0
 */
@NoImplement
public interface ConfigurationExtension {

}
