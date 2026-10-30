/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.profiling;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.FORCE_RUNTIME_PROFILING_CONSUMERS_ENABLEMENT_PROPERTY;
import static com.zernova.runtime.feature.internal.togglz.ZernovaTogglzFeatureManagerProvider.FEATURE_PROVIDER;
import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.setFeatureState;
import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.withFeatureUser;

import static java.util.Arrays.asList;

import com.zernova.runtime.api.profiling.ProfilingDataConsumer;
import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.feature.internal.togglz.user.ZernovaTogglzArtifactFeatureUser;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.util.ArrayList;
import java.util.List;

import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

/**
 * Enables the {@link ProfilingDataConsumer}'s as internal runtime profilers so that they will consume profiling data by default
 * without using the feature toggling mechanism.
 *
 * The {@link ProfilingDataConsumer}'s are disabled after the test.
 */
public class EnableInternalRuntimeProfilers extends SystemProperty {

  private List<ProfilingDataConsumer<?>> profilingDataConsumers = new ArrayList<>();

  public EnableInternalRuntimeProfilers(ProfilingDataConsumer<?>... profilingDataConsumers) {
    super(FORCE_RUNTIME_PROFILING_CONSUMERS_ENABLEMENT_PROPERTY, "true");
    this.profilingDataConsumers = asList(profilingDataConsumers);
  }

  @Override
  protected void after() {
    super.after();
    for (ProfilingDataConsumer<?> profilingDataConsumer : profilingDataConsumers)
      for (ProfilingEventType<?> eventType : profilingDataConsumer.getProfilingEventTypes()) {
        disableProfilingEventTypeForConsumer(eventType, profilingDataConsumer.getClass().getName());
      }
  }

  private void disableProfilingEventTypeForConsumer(ProfilingEventType<?> profilingEventType, String profilingFeatureSuffix) {
    withFeatureUser(new ZernovaTogglzArtifactFeatureUser(""), () -> {
      Feature feature =
          FEATURE_PROVIDER.getOrRegisterProfilingTogglzFeatureFrom(profilingEventType, profilingFeatureSuffix);
      setFeatureState(new FeatureState(feature, false));
    });
  }

}
