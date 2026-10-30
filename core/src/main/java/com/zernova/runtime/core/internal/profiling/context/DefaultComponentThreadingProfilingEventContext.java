/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling.context;

import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.profiling.threading.ThreadSnapshot;
import com.zernova.runtime.core.api.event.CoreEvent;

import java.util.Optional;

/**
 * A {@link com.zernova.runtime.api.profiling.type.context.ComponentThreadingProfilingEventContext} that encapsulates data for the
 * profiling event.
 *
 * @since 4.4
 */
public class DefaultComponentThreadingProfilingEventContext
    implements com.zernova.runtime.api.profiling.type.context.ComponentThreadingProfilingEventContext {

  private final CoreEvent event;
  private final String artifactId;
  private final String artifactType;
  private final String threadName;
  private final long profilingEventTimestamp;
  private final Optional<ComponentLocation> location;
  private ThreadSnapshot snapshot;

  public DefaultComponentThreadingProfilingEventContext(CoreEvent event,
                                                        ComponentLocation location,
                                                        String threadName,
                                                        String artifactId,
                                                        String artifactType,
                                                        long profilingEventTimestamp) {
    this.event = event;
    this.threadName = threadName;
    this.artifactId = artifactId;
    this.artifactType = artifactType;
    this.profilingEventTimestamp = profilingEventTimestamp;
    this.location = ofNullable(location);
  }

  @Override
  public String getCorrelationId() {
    return event.getCorrelationId();
  }

  @Override
  public String getThreadName() {
    return threadName;
  }

  @Override
  public String getArtifactId() {
    return artifactId;
  }

  @Override
  public String getArtifactType() {
    return artifactType;
  }

  @Override
  public long getTriggerTimestamp() {
    return profilingEventTimestamp;
  }

  @Override
  public Optional<ComponentLocation> getLocation() {
    return location;
  }

  @Override
  public Optional<ThreadSnapshot> getThreadSnapshot() {
    return ofNullable(snapshot);
  }

  @Override
  public void setThreadSnapshot(ThreadSnapshot threadSnapshot) {
    this.snapshot = threadSnapshot;
  }
}
