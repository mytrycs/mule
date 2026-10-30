/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.management.stats;

import static java.lang.System.currentTimeMillis;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.core.api.construct.FlowConstruct;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Accumulates the processing time for all branches of a flow
 */
@NoExtend
public class ProcessingTime implements Serializable {

  /**
   * Serial version
   */
  private static final long serialVersionUID = 1L;

  private final AtomicLong accumulator = new AtomicLong();
  private final FlowConstructStatistics statistics;

  /**
   * Create a ProcessingTime for the specified ZernovaSession.
   *
   * @return ProcessingTime if the session has an enabled FlowConstructStatistics or null otherwise
   */
  public static ProcessingTime newInstance(FlowConstruct flow) {
    FlowConstructStatistics stats = flow.getStatistics();
    if (stats != null && flow.getStatistics().isEnabled()) {
      return new ProcessingTime(stats, flow.getMuleContext().getProcessorTimeWatcher());
    } else {
      return null;
    }
  }

  /**
   * Create a Processing Time
   *
   * @param stats       never null
   * @param muleContext
   */
  private ProcessingTime(FlowConstructStatistics stats, ProcessingTimeWatcher processorTimeWatcher) {
    this.statistics = stats;
    processorTimeWatcher.addProcessingTime(this);
  }

  /**
   * Add the execution time for this branch to the flow construct's statistics
   *
   * @param startTime time this branch started
   */
  public void addFlowExecutionBranchTime(long startTime) {
    if (statistics.isEnabled()) {
      long elapsedTime = getEffectiveTime(currentTimeMillis() - startTime);
      statistics.addFlowExecutionBranchTime(elapsedTime, accumulator.addAndGet(elapsedTime));
    }
  }

  /**
   * Convert processing time to effective processing time. If processing took less than a tick, we consider it to have been one
   * millisecond
   */
  public static long getEffectiveTime(long time) {
    return (time <= 0) ? 1L : time;
  }

  public FlowConstructStatistics getStatistics() {
    return statistics;
  }

  public AtomicLong getAccumulator() {
    return accumulator;
  }
}
