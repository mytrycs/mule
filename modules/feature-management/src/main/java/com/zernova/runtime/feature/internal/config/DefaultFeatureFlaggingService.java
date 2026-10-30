/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.config;

import static com.zernova.runtime.feature.internal.togglz.ZernovaTogglzFeatureManagerProvider.FEATURE_PROVIDER;
import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.addMuleTogglzRuntimeFeature;
import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.getFeatureState;
import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.getTogglzManagedArtifactFeatures;
import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.setFeatureState;
import static com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzFeatureFlaggingUtils.withFeatureUser;

import com.zernova.runtime.api.profiling.ProfilingProducerScope;
import com.zernova.runtime.api.profiling.type.ProfilingEventType;
import com.zernova.runtime.feature.internal.config.profiling.ZernovaProfilingDataProducerFeatureStatus;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingDataProducerStatus;
import com.zernova.runtime.feature.internal.config.profiling.ProfilingFeatureFlaggingService;
import com.zernova.runtime.feature.internal.togglz.config.ZernovaTogglzManagedArtifactFeatures;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.feature.internal.togglz.user.ZernovaTogglzArtifactFeatureUser;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;

import java.util.Map;
import java.util.Set;


/**
 * Default implementation of {@code FeatureFlaggingService}
 */
public class DefaultFeatureFlaggingService implements ProfilingFeatureFlaggingService, Disposable {

  private final FeatureUser featureUser;
  private final ZernovaTogglzManagedArtifactFeatures features;

  public DefaultFeatureFlaggingService(String artifactId, Map<com.zernova.runtime.api.config.Feature, Boolean> features) {
    registerFeatures(features.keySet());
    featureUser = new ZernovaTogglzArtifactFeatureUser(artifactId);
    this.features = getTogglzManagedArtifactFeatures(artifactId, features);
  }

  private void registerFeatures(Set<com.zernova.runtime.api.config.Feature> features) {
    for (com.zernova.runtime.api.config.Feature runtimeFeature : features) {
      addMuleTogglzRuntimeFeature(runtimeFeature);
    }
  }

  @Override
  public boolean isEnabled(com.zernova.runtime.api.config.Feature feature) {
    Feature togglzFeature = FEATURE_PROVIDER.getRuntimeTogglzFeature(feature);

    // If the feature state is not precalculated for this context, it is calculated.
    if (!features.containsKey(togglzFeature)) {
      return FeatureContext.getFeatureManager().isActive(togglzFeature);
    }

    return features.get(togglzFeature).isEnabled();
  }

  @Override
  public void dispose() {
    features.dispose();
  }

  @Override
  public ProfilingDataProducerStatus getProfilingDataProducerStatus(ProfilingEventType<?> profilingEventType,
                                                                    ProfilingProducerScope profilingProducerContext) {
    return new ZernovaProfilingDataProducerFeatureStatus(profilingEventType, FEATURE_PROVIDER, featureUser);
  }

  @Override
  public void registerProfilingFeature(ProfilingEventType<?> profilingEventType, String identifier) {
    withFeatureUser(featureUser, () -> {
      Feature feature =
          FEATURE_PROVIDER.getOrRegisterProfilingTogglzFeatureFrom(profilingEventType, identifier);
      setFeatureState(getFeatureState(feature));
    });
  }

  @Override
  public void toggleProfilingFeature(ProfilingEventType<?> profilingEventType, String profilingFeatureSuffix, boolean status) {
    withFeatureUser(featureUser, () -> {
      Feature feature =
          FEATURE_PROVIDER.getOrRegisterProfilingTogglzFeatureFrom(profilingEventType, profilingFeatureSuffix);
      setFeatureState(new FeatureState(feature, status));
    });
  }

  public String getArtfactName() {
    return featureUser.getName();
  }
}
