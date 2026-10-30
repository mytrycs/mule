/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.streaming.object;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.streaming.Cursor;
import com.zernova.runtime.api.streaming.HasSize;

import java.util.Iterator;

/**
 * An {@link Iterator} which subscribes to the {@link Cursor} contract
 *
 * @since 1.0
 */
@NoImplement
public interface CursorIterator<T> extends Iterator<T>, Cursor, HasSize {

}
