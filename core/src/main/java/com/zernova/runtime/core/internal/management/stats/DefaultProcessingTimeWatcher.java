/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.management.stats;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.management.stats.FlowConstructStatistics;
import com.zernova.runtime.core.api.management.stats.ProcessingTime;
import com.zernova.runtime.core.api.management.stats.ProcessingTimeWatcher;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.inject.Inject;

public class DefaultProcessingTimeWatcher implements ProcessingTimeWatcher, ZernovaContextAware {

  private final ReferenceQueue<ProcessingTime> queue = new ReferenceQueue<>();
  private final Map<ProcessingTimeReference, Object> refs = new ConcurrentHashMap<>();
  private ZernovaContext muleContext;
  private Scheduler scheduler;
  private Future<?> checkerTask;

  @Override
  public void addProcessingTime(ProcessingTime processingTime) {
    refs.put(new ProcessingTimeReference(processingTime, queue), refs);
  }

  @Override
  public void start() throws ZernovaException {
    scheduler = muleContext.getSchedulerService().customScheduler(muleContext.getSchedulerBaseConfig()
        .withName("processing.time.monitor").withMaxConcurrentTasks(1).withShutdownTimeout(0, MILLISECONDS));
    checkerTask = scheduler.submit(new ProcessingTimeChecker());
  }

  @Override
  public void stop() throws ZernovaException {
    if (scheduler != null) {
      checkerTask.cancel(true);
      scheduler.stop();
    }
    refs.clear();
  }

  @Override
  @Inject
  public void setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }

  private class ProcessingTimeChecker implements Runnable {

    /**
     * As weak references to completed ProcessingTimes are delivered, record them
     */
    @Override
    public void run() {
      while (!currentThread().isInterrupted()) {
        try {
          ProcessingTimeReference ref = (ProcessingTimeReference) queue.remove();
          refs.remove(ref);

          FlowConstructStatistics stats = ref.getStatistics();
          if (stats.isEnabled()) {
            stats.addCompleteFlowExecutionTime(ref.getAccumulator().longValue());
          }
        } catch (InterruptedException ex) {
          currentThread().interrupt();
        }
      }
    }
  }

  /**
   * Weak reference that includes flow statistics to be updated
   */
  static class ProcessingTimeReference extends WeakReference<ProcessingTime> {

    private final FlowConstructStatistics statistics;
    private final AtomicLong accumulator;

    ProcessingTimeReference(ProcessingTime time, ReferenceQueue<ProcessingTime> queue) {
      super(time, queue);
      this.statistics = time.getStatistics();
      this.accumulator = time.getAccumulator();
    }

    public AtomicLong getAccumulator() {
      return accumulator;
    }

    public FlowConstructStatistics getStatistics() {
      return statistics;
    }
  }
}
