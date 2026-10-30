/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.streaming.bytes;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.streaming.Cursor;

import java.io.InputStream;

/**
 * An {@link InputStream} which subscribes to the {@link Cursor} contract
 *
 * @since 1.0
 */
@NoExtend
public abstract class CursorStream extends InputStream implements Cursor {

}
