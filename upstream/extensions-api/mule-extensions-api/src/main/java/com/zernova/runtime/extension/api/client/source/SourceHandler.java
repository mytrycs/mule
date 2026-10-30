/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.client.source;

import com.zernova.api.annotation.Experimental;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.extension.api.client.ExtensionsClient;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Offers management and lifecycle control over a message source created through the {@link ExtensionsClient}.
 * <p>
 * <b>NOTE:</b> Experimental feature. Backwards compatibility not guaranteed.
 *
 * @since 1.5.0
 */
@Experimental
@MinZernovaVersion("4.5.0")
public interface SourceHandler extends Startable, Stoppable, Disposable {

  /**
   * Starts the source, which means it will start producing messages.
   * <p>
   * Calling this method on an already started instance has no effect.
   *
   * @throws ZernovaException         if it fails to start
   * @throws IllegalStateException if {@link #dispose()} has already been invoked
   */
  @Override
  void start() throws ZernovaException;

  /**
   * Stops the source, which means it will stop producing messages. It can be restarted by invoking {@link #start()} again.
   * <p>
   * Calling this method on an already stopped instance has no effect.
   *
   * @throws ZernovaException         if it fails to stop
   * @throws IllegalStateException if {@link #dispose()} has already been invoked
   */
  @Override
  void stop() throws ZernovaException;

  /**
   * Completely disposes the source and all its allocated resources. It can't be restarted, a new instance needs to be created.
   *
   * Invoking this method on an already disposed instance has no effect.
   */
  @Override
  void dispose();
}
