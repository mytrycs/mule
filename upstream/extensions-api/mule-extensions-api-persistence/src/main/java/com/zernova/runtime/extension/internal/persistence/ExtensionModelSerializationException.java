/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.persistence;

import com.zernova.runtime.api.meta.model.ExtensionModel;

/**
 * {@link RuntimeException} to indicate that a problem occurred serializing or deserializing a {@link ExtensionModel}
 *
 * @since 1.0
 */
class ExtensionModelSerializationException extends RuntimeException {

  ExtensionModelSerializationException(String message, Exception cause) {
    super(message, cause);
  }
}
