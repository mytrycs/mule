/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck;

import static com.zernova.runtime.core.api.source.MessageSource.BackPressureStrategy.WAIT;
import static com.zernova.runtime.core.internal.util.rx.Operators.requestUnbounded;
import static reactor.core.publisher.Flux.just;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.source.MessageSource;
import com.zernova.runtime.core.api.util.ObjectUtils;
import com.zernova.runtime.core.internal.util.rx.Operators;

import org.reactivestreams.Publisher;

public class TriggerableMessageSource extends AbstractComponent implements MessageSource {

  protected Processor listener;
  private BackPressureStrategy backPressureStrategy = WAIT;

  public TriggerableMessageSource() {}

  public TriggerableMessageSource(BackPressureStrategy backPressureStrategy) {
    this.backPressureStrategy = backPressureStrategy;
  }

  public CoreEvent trigger(CoreEvent event) throws ZernovaException {
    return listener.process(event);
  }

  public Publisher<CoreEvent> triggerAsync(CoreEvent event) {
    return just(event).transform(listener);
  }

  @Override
  public void setListener(Processor listener) {
    this.listener = listener;
  }

  public Processor getListener() {
    return this.listener;
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }

  @Override
  public BackPressureStrategy getBackPressureStrategy() {
    return backPressureStrategy;
  }
}
