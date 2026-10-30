/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing.forkjoin;

import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.api.metadata.DataType.MULE_MESSAGE_MAP;

import static java.util.stream.Collectors.toMap;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.routing.ForkJoinStrategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

import jakarta.inject.Inject;

/**
 * {@link ForkJoinStrategy} that:
 * <ul>
 * <li>Performs parallel execution of route pairs subject to {@code maxConcurrency}.
 * <li>Merges variables using a last-wins strategy.
 * <li>Waits for the completion of all routes before emitting a result event, with an optional timeout.
 * <li>Collects results into a result {@link CoreEvent} with a {@link java.util.Map} collection that contains {@link String} as
 * key and {@link com.zernova.runtime.api.message.Message} as value in the payload where the {@link java.util.Map} key is a string
 * representation of the sequence number of the
 * {@link ForkJoinStrategy#com.zernova.runtime.core.internal.routing.ForkJoinStrategy.RoutingPair}.
 * <li>Will processor all routes, regardless of errors, and propagating a composite exception where there were one or more errors.
 * </ul>
 */
public class CollectMapForkJoinStrategyFactory extends AbstractForkJoinStrategyFactory {

  @Inject
  public CollectMapForkJoinStrategyFactory(FeatureFlaggingService featureFlaggingService) {
    super(featureFlaggingService);
  }

  @Override
  protected Function<List<CoreEvent>, CoreEvent> createResultEvent(CoreEvent original,
                                                                   CoreEvent.Builder resultBuilder) {
    return list -> resultBuilder
        .message(of(list.stream().collect(toMap(event -> Integer.toString(event.getGroupCorrelation().get().getSequence()),
                                                event -> event.getMessage(),
                                                (a, b) -> b,
                                                LinkedHashMap::new))))
        .build();
  }

  @Override
  public DataType getResultDataType() {
    return MULE_MESSAGE_MAP;
  }

}
