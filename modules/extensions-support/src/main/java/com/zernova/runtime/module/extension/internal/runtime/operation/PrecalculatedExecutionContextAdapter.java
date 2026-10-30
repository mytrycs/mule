/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation;

import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.AbstractExecutionContextAdapterDecorator;

import java.util.Optional;

class PrecalculatedExecutionContextAdapter<M extends ComponentModel> extends AbstractExecutionContextAdapterDecorator<M> {

  private Optional<ConfigurationInstance> configuration;

  PrecalculatedExecutionContextAdapter(ExecutionContextAdapter<M> decorated) {
    super(decorated);

    configuration = decorated.getConfiguration();
  }

  @Override
  public Optional<ConfigurationInstance> getConfiguration() {
    return configuration;
  }
}
