/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling.threading;

import static java.lang.Thread.currentThread;
import static java.lang.management.ManagementFactory.getThreadMXBean;

import com.zernova.runtime.api.profiling.threading.ThreadSnapshot;
import com.zernova.runtime.api.profiling.threading.ThreadSnapshotCollector;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class JvmThreadSnapshotCollector implements ThreadSnapshotCollector {

  private static final ThreadMXBean threadMXBean = getThreadMXBean();

  public JvmThreadSnapshotCollector() {}

  // TODO: Evaluate moving this to a better place
  static {
    threadMXBean.setThreadContentionMonitoringEnabled(true);
    threadMXBean.setThreadCpuTimeEnabled(true);
  }

  @Override
  public ThreadSnapshot getCurrentThreadSnapshot() {
    final long id = currentThread().getId();
    ThreadInfo threadInfo = threadMXBean.getThreadInfo(id);
    return DefaultThreadSnapshot.builder()
        .withBlockedTime(threadInfo.getBlockedTime())
        .withWaitedTime(threadInfo.getWaitedTime())
        .withCPUTime(threadMXBean.getThreadCpuTime(id))
        .build();
  }
}
