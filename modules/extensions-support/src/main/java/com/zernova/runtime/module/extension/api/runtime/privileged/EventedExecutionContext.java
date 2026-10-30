/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.runtime.privileged;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;

/**
 * A specialization of {@link ExecutionContext} which contains an {@link CoreEvent}
 *
 * @param <M> the generic type of of the model which represents the component being executed
 * @since 4.0
 */
@NoImplement
public interface EventedExecutionContext<M extends ComponentModel> extends ExecutionContext<M> {

  /**
   * Returns the {@link CoreEvent} on which an operation is to be executed
   */
  CoreEvent getEvent();

  /**
   * Changes the {@link CoreEvent} on which an operation is to be executed. Not null.
   *
   * @param updated the event to use
   */
  void changeEvent(CoreEvent updated);

}
