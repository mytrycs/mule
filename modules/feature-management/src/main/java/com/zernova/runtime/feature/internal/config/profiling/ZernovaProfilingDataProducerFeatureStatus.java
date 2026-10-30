/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.config.profiling;

import static java.util.concurrent.ConcurrentHashMap.newKeySet;
import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.getFeatureState;
import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.withFeatureUser;

import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.feature.internal.togglz.ZernovaTogglzProfilingFeature;
import com.zernova.runtime.feature.internal.togglz.provider.DefaultZernovaTogglzFeatureProvider;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

import java.util.Collection;
import java.util.Set;

/**
 * The status of a {@link com.zernova.runtime.api.profiling.ProfilingDataProducer} according to the requirements from the
 * {@link com.zernova.runtime.api.profiling.ProfilingDataConsumer}
 */
public class ZernovaProfilingDataProducerFeatureStatus implements ProfilingDataProducerStatus {

  private final Set<FeatureState> profilingFeaturesStates = newKeySet();
  private final DefaultZernovaTogglzFeatureProvider featureProvider;
  private final ProfilingEventType<?> profilingEventType;
  private final FeatureUser featureUser;

  public ZernovaProfilingDataProducerFeatureStatus(ProfilingEventType<?> profilingEventType,
                                                DefaultZernovaTogglzFeatureProvider featureProvider,
                                                FeatureUser featureUser) {
    this.featureProvider = featureProvider;
    this.profilingEventType = profilingEventType;
    this.featureUser = featureUser;
    resetFeatureStates();
  }

  private void resetFeatureStates() {

    withFeatureUser(featureUser, () -> {
      Collection<ZernovaTogglzProfilingFeature> profilingFeatures = featureProvider.getConsumerFeaturesFor(profilingEventType);
      for (Feature feature : profilingFeatures) {
        profilingFeaturesStates.add(getFeatureState(feature));
      }
    });
  }

  @Override
  public boolean isEnabled() {
    return profilingFeaturesStates.stream().anyMatch(FeatureState::isEnabled);
  }

  @Override
  public void reset() {
    resetFeatureStates();
  }
}
