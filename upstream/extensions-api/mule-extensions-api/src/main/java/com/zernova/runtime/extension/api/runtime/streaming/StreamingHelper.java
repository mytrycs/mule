/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.streaming;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.streaming.Cursor;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.api.streaming.bytes.CursorStreamProvider;
import com.zernova.runtime.api.streaming.object.CursorIteratorProvider;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.io.InputStream;
import java.util.Map;

/**
 * This class provides helper methods to deal with repeatable streaming resources which are contained into structures of arbitrary
 * complexity.
 * <p>
 * As you know, when a parameter is resolved to a {@link CursorProvider}, the runtime automatically obtains a {@link Cursor} and
 * injects that value instead. However, if that provider is embedded as the value of a {@link Map} or an arbitrary pojo, the
 * runtime can't guess that's the case and go resolve it.
 * <p>
 * Same thing applies when a component is producing a result. The runtime automatically converts returned {@link InputStream} or
 * {@link PagingProvider} instances into {@link CursorProvider} ones. However, if such instances are contained in some other
 * value, that resolution won't happen automatically either.
 * <p>
 * For these border cases, this class provides some utilities to adapt the {@link CursorProvider providers} into {@link Cursor
 * cursors} and vice versa
 *
 * @see CursorProvider
 * @see CursorStreamProvider
 * @see CursorIteratorProvider
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@NoImplement
public interface StreamingHelper extends com.zernova.sdk.api.runtime.streaming.StreamingHelper {

}
