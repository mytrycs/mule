/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling;

import com.zernova.runtime.api.profiling.ProfilingProducerScopeType;
import com.zernova.runtime.api.profiling.ProfilingProducerScope;

import java.util.Objects;

import static com.zernova.runtime.api.profiling.ProfilingProducerScopeType.ARTIFACT_SCOPE_TYPE;

/**
 * A {@link ProfilingProducerScope} that let us retrieve {@link com.zernova.runtime.api.profiling.ProfilingDataProducer} associated
 * to an artifact.
 *
 * @since 4.5.0
 */
public class ArtifactProfilingProducerScope implements ProfilingProducerScope {

  private final String artifactIdentifier;

  public ArtifactProfilingProducerScope(String artifactIdentifier) {
    this.artifactIdentifier = artifactIdentifier;
  }

  @Override
  public String getProducerScopeIdentifier() {
    return artifactIdentifier;
  }

  @Override
  public ProfilingProducerScopeType getProducerScopeTypeIdentifier() {
    return ARTIFACT_SCOPE_TYPE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ArtifactProfilingProducerScope that = (ArtifactProfilingProducerScope) o;
    return Objects.equals(artifactIdentifier, that.artifactIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactIdentifier);
  }
}
