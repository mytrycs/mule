/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.policy;

import static java.util.Optional.ofNullable;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.getDefaultProcessingStrategyFactory;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.policy.PolicyChain;
import com.zernova.runtime.core.api.policy.PolicyInstance;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import com.zernova.runtime.core.api.util.UUID;
import com.zernova.runtime.core.internal.lifecycle.DefaultLifecycleManager;

import java.util.Optional;

import org.slf4j.Logger;

@NoExtend
public class DefaultPolicyInstance extends AbstractComponent
    implements PolicyInstance, ZernovaContextAware, Lifecycle {

  private static final Logger LOGGER = getLogger(DefaultPolicyInstance.class);

  private ProcessingStrategy processingStrategy;

  private String name;

  private PolicyChain operationPolicyChain;
  private PolicyChain sourcePolicyChain;

  private ZernovaContext muleContext;
  private final DefaultLifecycleManager<DefaultPolicyInstance> lifecycleStateManager =
      new DefaultLifecycleManager<>("proxy-policy-" + UUID.getUUID(), this);

  @Override
  public void initialise() throws InitialisationException {
    processingStrategy = defaultProcessingStrategy().create(muleContext, getName());

    if (operationPolicyChain != null) {
      operationPolicyChain.setProcessingStrategy(processingStrategy);
    }
    if (sourcePolicyChain != null) {
      sourcePolicyChain.setProcessingStrategy(processingStrategy);
    }

    initialiseIfNeeded(operationPolicyChain, muleContext);
    initialiseIfNeeded(sourcePolicyChain, muleContext);
    lifecycleStateManager.fireInitialisePhase((phaseNam, object) -> {
    });
  }

  private ProcessingStrategyFactory defaultProcessingStrategy() {
    return getDefaultProcessingStrategyFactory(muleContext);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(processingStrategy);
    startIfNeeded(operationPolicyChain);
    startIfNeeded(sourcePolicyChain);
    lifecycleStateManager.fireStartPhase((phaseNam, object) -> {
    });

  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setMuleContext(ZernovaContext context) {
    this.muleContext = context;
  }

  @Override
  public void dispose() {
    disposeIfNeeded(operationPolicyChain, LOGGER);
    disposeIfNeeded(sourcePolicyChain, LOGGER);
    disposeIfNeeded(processingStrategy, LOGGER);
    lifecycleStateManager.fireDisposePhase((phaseNam, object) -> {
    });
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(operationPolicyChain);
    stopIfNeeded(sourcePolicyChain);
    stopIfNeeded(processingStrategy);
    lifecycleStateManager.fireStopPhase((phaseNam, object) -> {
    });
  }

  public void setOperationPolicyChain(PolicyChain request) {
    this.operationPolicyChain = request;
  }

  public void setSourcePolicyChain(PolicyChain source) {
    this.sourcePolicyChain = source;
  }

  @Override
  public Optional<PolicyChain> getSourcePolicyChain() {
    return ofNullable(sourcePolicyChain);
  }

  @Override
  public Optional<PolicyChain> getOperationPolicyChain() {
    return ofNullable(operationPolicyChain);
  }

  @Override
  public ProcessingStrategy getProcessingStrategy() {
    return processingStrategy;
  }
}
