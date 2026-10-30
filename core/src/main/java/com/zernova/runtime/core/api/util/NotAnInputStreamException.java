/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.util;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;

import java.io.InputStream;

/**
 * Indicates that an {@link InputStream} or {@link CursorStreamProvider} was expected but something else was found.
 *
 * @since 4.0
 */
public final class NotAnInputStreamException extends ZernovaException {

  public NotAnInputStreamException(Object value) {
    super(createStaticMessage("Was expecting an InputStream or a stream provider but " +
        (value != null ? value.getClass().getName() : "null") +
        " was found instead"));
  }
}
