/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.processor;

import static com.zernova.runtime.core.api.rx.Exceptions.checkedFunction;
import static com.zernova.runtime.core.internal.util.rx.Operators.nullSafeMap;
import static reactor.core.publisher.Flux.from;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.event.CoreEvent;

import org.reactivestreams.Publisher;

/**
 * Processes {@link CoreEvent}'s. Implementations that do not mutate the {@link CoreEvent} or pass it on to another
 * MessageProcessor should return the ZernovaEvent they receive.
 * <p>
 * From 4.0 this interface also extends {@link ReactiveProcessor} and implementations of this interface can be used in
 * {@link CoreEvent} stream processing via the default implementation of {@link #apply(Publisher)} that performs a map function on
 * the stream using the result of the invocation of the blocking {@link #process(CoreEvent)} method. Using this approach simple
 * processor implementations that don't block or perform blocking IO can continue to implement {@link Processor} and require no
 * changes.
 *
 * @since 3.0
 */
public interface Processor extends ReactiveProcessor {

  /**
   * Invokes the MessageProcessor.
   *
   * @param event ZernovaEvent to be processed
   * @return optional response ZernovaEvent
   * @throws ZernovaException
   */
  CoreEvent process(CoreEvent event) throws ZernovaException;

  /**
   * Applies a {@code Publisher<CoreEvent>} function transforming a stream of {@link CoreEvent}'s.
   * <p>
   * The default implementation delegates to {@link #process(CoreEvent)} and will:
   * <ol>
   * <li>propagate any exception thrown</li>
   * <li>drop events if invocation of {@link #process(CoreEvent)} returns null.</li>
   * </ol>
   *
   * @param publisher the event stream to transform
   * @return the transformed event stream
   */
  @Override
  default Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    return from(publisher).handle(nullSafeMap(checkedFunction(this::process)));
  }

}
