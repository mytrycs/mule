/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.enricher;

import com.zernova.runtime.extension.api.error.ErrorTypeDefinition;

/**
 * This can't be moved to an inner Enum due to compilation issues
 */
public enum LevelErrorTypes implements ErrorTypeDefinition<LevelErrorTypes> {
  OPERATION, EXTENSION, CONSTRUCT
}
