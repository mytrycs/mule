/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.component;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.util.StringUtils;

/**
 * Reads {@link CoreEvent} from a test connector's queue.
 */
public class QueueReaderMessageProcessor implements Processor {

  private final String queueName;
  private final Long timeout;
  private final TestConnectorQueueHandler queueHandler;

  /**
   * Creates a queue reader
   *
   * @param registry  application's mule context. Not null.
   * @param queueName name of the queue to use. Non empty
   * @param timeout   number of milliseconds to wait for an available event. Non negative. Null means no timeout required.
   */
  public QueueReaderMessageProcessor(Registry registry, String queueName, Long timeout) {
    checkArgument(!StringUtils.isEmpty(queueName), "Queue name cannot be empty");
    if (timeout != null) {
      checkArgument(timeout >= 0L, "Timeout cannot be negative");
    }

    this.queueHandler = new TestConnectorQueueHandler(registry);
    this.queueName = queueName;
    this.timeout = timeout;
  }

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    if (timeout == null) {
      return queueHandler.read(queueName);
    } else {
      return queueHandler.read(queueName, timeout);
    }
  }
}
