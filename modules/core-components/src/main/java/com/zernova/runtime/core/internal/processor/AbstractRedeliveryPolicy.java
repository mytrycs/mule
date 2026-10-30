/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor;

import static java.util.Collections.singletonList;
import static com.zernova.runtime.core.api.config.i18n.CoreMessages.initialisationFailure;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.buildNewChainWithListOfProcessors;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.getProcessingStrategy;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.exception.NullExceptionHandler;
import com.zernova.runtime.core.api.processor.AbstractMessageProcessorOwner;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.privileged.processor.Scope;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;

import java.util.List;

/**
 * Implement a redelivery policy for Mule. This is similar to JMS retry policies that will redeliver a message a maximum number of
 * times. If this maximum is exceeded, the message is sent to a dead letter queue, Here, if the processing of the messages fails
 * too often, the message is sent to the failedMessageProcessor MP, whence success is force to be returned, to allow the message
 * to be considered "consumed".
 */
@NoExtend
public abstract class AbstractRedeliveryPolicy extends AbstractMessageProcessorOwner implements Scope {

  private List<Processor> processors;
  protected MessageProcessorChain nestedChain;

  protected int maxRedeliveryCount;
  public static final int REDELIVERY_FAIL_ON_FIRST = 0;

  @Override
  public void initialise() throws InitialisationException {
    if (maxRedeliveryCount < 0) {
      throw new InitialisationException(initialisationFailure("maxRedeliveryCount must be positive"), this);
    }
    this.nestedChain = buildNewChainWithListOfProcessors(getProcessingStrategy(locator, this), processors,
                                                         NullExceptionHandler.getInstance());
    super.initialise();
  }

  public int getMaxRedeliveryCount() {
    return maxRedeliveryCount;
  }

  public void setMaxRedeliveryCount(int maxRedeliveryCount) {
    this.maxRedeliveryCount = maxRedeliveryCount;
  }

  public void setMessageProcessors(List<Processor> processors) {
    this.processors = processors;
  }

  @Override
  protected List<Processor> getOwnedMessageProcessors() {
    return singletonList(nestedChain);
  }
}
