/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config;

import static java.lang.Boolean.getBoolean;
import static java.lang.System.getProperty;
import static com.zernova.runtime.core.internal.processor.strategy.util.ProfilingUtils.getArtifactId;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.zernova.runtime.api.config.Feature;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.core.api.config.FeatureContext;
import com.zernova.runtime.feature.internal.config.DefaultFeatureFlaggingService;
import org.slf4j.Logger;

/**
 * This builder creates a {@link FeatureFlaggingService} whose {@link Feature} flags are set by evaluating the features registered
 * via {@link com.zernova.runtime.core.api.config.FeatureFlaggingRegistry#registerFeatureFlag(Feature, Predicate)} against a
 * {@link FeatureContext}.
 * <p>
 * Maintains backward compatibility with a legacy {@link com.zernova.runtime.api.meta.ZernovaVersion} evaluation.
 *
 * @see FeatureFlaggingService
 * @see com.zernova.runtime.core.api.config.FeatureFlaggingRegistry
 *
 * @since 4.4.0
 */
public final class FeatureFlaggingServiceBuilder {

  private static final Logger LOGGER = getLogger(FeatureFlaggingServiceBuilder.class);

  private ZernovaContext muleContext;
  private FeatureContext featureContext;
  private String artifactName = "";

  private final Map<Feature, Predicate<FeatureContext>> featureContextFlags = new HashMap<>();
  private final Map<Feature, Predicate<ZernovaContext>> muleContextFlags = new HashMap<>();

  /**
   * Sets the {@link ZernovaContext} that will be used for the legacy {@link #withMuleContextFlags(Map)} evaluation.
   *
   * @param muleContext {@link ZernovaContext} that will be used for the legacy {@link #withMuleContextFlags(Map)} evaluation.
   * @return This {@link FeatureFlaggingServiceBuilder}.
   */
  public FeatureFlaggingServiceBuilder withContext(ZernovaContext muleContext) {
    if (this.artifactName.isEmpty()) {
      this.artifactName = muleContext.getId();
    }
    this.muleContext = muleContext;
    return this;
  }

  /**
   * Sets the {@link FeatureContext} that will be used for the {@link #withFeatureContextFlags(Map)} evaluation.
   *
   * @param featureContext {@link FeatureContext} that will be used for the legacy {@link #withFeatureContextFlags(Map)}
   *                       evaluation.
   * @return This {@link FeatureFlaggingServiceBuilder}.
   */
  public FeatureFlaggingServiceBuilder withContext(FeatureContext featureContext) {
    this.artifactName = featureContext.getArtifactName();
    this.featureContext = featureContext;
    return this;
  }

  /**
   * Features whose flags will be set by evaluating a predicate against a {@link FeatureContext}.
   *
   * @param configurations The features and their corresponding predicates.
   * @return This {@link FeatureFlaggingServiceBuilder}.
   */
  public FeatureFlaggingServiceBuilder withFeatureContextFlags(Map<Feature, Predicate<FeatureContext>> configurations) {
    this.featureContextFlags.putAll(configurations);
    return this;
  }

  /**
   * Features whose flags will be set by evaluating a predicate against a {@link ZernovaContext}.
   *
   * @param configurations The features and their corresponding predicates.
   * @return This {@link FeatureFlaggingServiceBuilder}.
   */
  public FeatureFlaggingServiceBuilder withMuleContextFlags(Map<Feature, Predicate<ZernovaContext>> configurations) {
    this.muleContextFlags.putAll(configurations);
    return this;
  }

  /**
   * Creates the {@link FeatureFlaggingService} instance configured by this {@link FeatureFlaggingServiceBuilder}.
   *
   * @return The {@link FeatureFlaggingService} instance.
   */
  public FeatureFlaggingService build() {
    Map<Feature, Boolean> features = new HashMap<>();
    LOGGER.debug("Configuring feature flags...");
    if (muleContext != null) {
      muleContextFlags.forEach((feature, artifactDescriptorPredicate) -> features
          .put(feature, isFeatureFlagEnabled(feature, muleContext, artifactDescriptorPredicate)));
    }
    if (featureContext != null) {
      featureContextFlags.forEach((feature, artifactDescriptorPredicate) -> features
          .put(feature, isFeatureFlagEnabled(feature, featureContext, artifactDescriptorPredicate)));
    }

    return new DefaultFeatureFlaggingService(artifactName, features);
  }

  /**
   * True if a feature flag is enabled under a determined feature context.
   *
   * @param feature          The feature whose feature flag must be evaluated.
   * @param featureContext   The feature context that must be used to set the feature flag.
   * @param featurePredicate The predicate that will be evaluated against the feature context.
   * @return True if a feature flag is enabled.
   */
  private <T> boolean isFeatureFlagEnabled(Feature feature, T featureContext, Predicate<T> featurePredicate) {
    boolean enabled;
    Optional<String> systemPropertyName = feature.getOverridingSystemPropertyName();
    if (systemPropertyName.isPresent() && getProperty(systemPropertyName.get()) != null) {
      enabled = getBoolean(systemPropertyName.get());
      LOGGER.debug("Setting feature {} = {} for artifact [{}] because of System Property '{}'", feature, enabled,
                   artifactName,
                   systemPropertyName);
    } else {
      enabled = featurePredicate.test(featureContext);
      LOGGER.debug("Setting feature {} = {} for artifact [{}]", feature, enabled, artifactName);
    }
    return enabled;
  }

}
