/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor.chain;

import com.zernova.runtime.api.component.execution.ExecutableComponent;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.privileged.processor.Scope;

import java.util.List;

/**
 * A chain of {@link Processor}'s. All implementations should propagate {@link ZernovaContext}, {@link FlowConstruct} and lifecycle
 * to {@link Processor}'s in the chains.
 */
public interface MessageProcessorChain extends ExecutableComponent, Lifecycle, ZernovaContextAware, Scope {

  /**
   * Obtain the list of {@link Processor}'s that this chains was created from.
   *
   * @return list of processors.
   */
  List<Processor> getMessageProcessors();

}
