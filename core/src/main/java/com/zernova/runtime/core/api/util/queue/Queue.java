/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.util.queue;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.NamedObject;

import java.io.Serializable;

/**
 * Interface for mule queues used for VM.
 */
@NoImplement
public interface Queue extends NamedObject {

  /**
   * Returns the number of elements in this queue.
   */
  int size();

  /**
   * Puts a new object in this queue and wait if necessary.
   */
  void put(Serializable object) throws InterruptedException;

  /**
   * Blocks and retrieves an object from this queue.
   *
   * @return an object.
   */
  Serializable take() throws InterruptedException;

  void untake(Serializable item) throws InterruptedException;

  Serializable peek() throws InterruptedException;

  Serializable poll(long timeout) throws InterruptedException;

  boolean offer(Serializable object, long timeout) throws InterruptedException;

  /**
   * Discards all the elements in the queue
   *
   * @throws InterruptedException
   */
  void clear() throws InterruptedException;

  /**
   * Disposes this queue by releasing it's storage and associated memory and storage. If after disposing the queue you try go get
   * it back, you'll get a fresh new one which maintains none of the original one's data
   *
   * @throws ZernovaException
   * @throws InterruptedException
   */
  void dispose() throws ZernovaException, InterruptedException;

}
