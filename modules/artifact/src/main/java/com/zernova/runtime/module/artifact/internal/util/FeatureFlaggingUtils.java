/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.internal.util;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import static java.lang.Boolean.getBoolean;
import static java.lang.System.getProperty;
import static java.util.Objects.requireNonNull;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.config.Feature;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.config.FeatureContext;
import com.zernova.runtime.core.api.config.FeatureFlaggingRegistry;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;

/**
 * Utility class meant to provide a {@link com.zernova.runtime.api.config.FeatureFlaggingService} substitute during the earlier
 * stages of the deployment, when such service is not available yet.
 *
 * @since 4.4.0
 */
public class FeatureFlaggingUtils {

  private static final Logger LOGGER = getLogger(FeatureFlaggingUtils.class);

  private FeatureFlaggingUtils() {}

  private static final LoadingCache<ArtifactDescriptor, FeatureFlaggingService> featureFlags = newBuilder()
      .weakKeys()
      .build(FeatureFlaggingUtils::buildFeatureFlaggingService);

  /**
   * True if a {@link Feature} is enabled, assuming that the given {@link ArtifactDescriptor} provides relevant
   * {@link FeatureContext} metadata.
   *
   * @param feature            The {@link Feature}.
   * @param artifactDescriptor Relevant {@link ArtifactDescriptor}.
   * @return True if the {@link Feature} must be enabled.
   */
  public static boolean isFeatureEnabled(Feature feature, ArtifactDescriptor artifactDescriptor) {
    return requireNonNull(featureFlags.get(artifactDescriptor)).isEnabled(feature);
  }

  /**
   * Constructs a {@link FeatureFlaggingService} instance containing all the registered {@link Feature} flags.
   *
   * @param artifactDescriptor The {@link ArtifactDescriptor} whose metadata will be used to create the {@link FeatureContext}.
   * @return A {@link FeatureFlaggingService}.
   * @see FeatureFlaggingRegistry
   */
  private static FeatureFlaggingService buildFeatureFlaggingService(ArtifactDescriptor artifactDescriptor) {
    Map<Feature, Boolean> features = new HashMap<>();
    LOGGER.debug("Configuring feature flags for artifact [{}]", artifactDescriptor.getName());
    FeatureContext featureContext = new FeatureContext(artifactDescriptor.getMinMuleVersion(), artifactDescriptor.getName());
    FeatureFlaggingRegistry.getInstance().getFeatureFlagConfigurations()
        .forEach((feature, featureContextPredicate) -> features.put(feature, evaluateFeatureFlag(feature, featureContext)));
    return new DeploymentFeatureFlaggingService(features);
  }

  /**
   * Determines a {@link Feature} flag value.
   *
   * @param feature        The {@link Feature} to evaluate.
   * @param featureContext Relevant {@link FeatureContext}.
   * @return True if the {@link Feature} is enabled.
   */
  private static boolean evaluateFeatureFlag(Feature feature, FeatureContext featureContext) {
    boolean enabled;
    Optional<String> systemPropertyName = feature.getOverridingSystemPropertyName();
    if (systemPropertyName.isPresent() && getProperty(systemPropertyName.get()) != null) {
      enabled = getBoolean(systemPropertyName.get());
      LOGGER.debug("Setting feature {} = {} for artifact [{}] because of System Property '{}'", feature, enabled,
                   featureContext.getArtifactName(),
                   systemPropertyName);
    } else {
      enabled = FeatureFlaggingRegistry.getInstance().getFeatureFlagConfigurations().get(feature).test(featureContext);
      LOGGER.debug("Setting feature {} = {} for artifact [{}]", feature, enabled, featureContext.getArtifactName());
    }
    return enabled;
  }

  /**
   * Internal {@link FeatureFlaggingService} implementation.
   */
  private static class DeploymentFeatureFlaggingService implements FeatureFlaggingService {

    private final Map<Feature, Boolean> featureFlags;

    public DeploymentFeatureFlaggingService(Map<Feature, Boolean> featureFlags) {
      this.featureFlags = featureFlags;
    }

    @Override
    public boolean isEnabled(Feature feature) {
      if (!featureFlags.containsKey(feature)) {
        throw new ZernovaRuntimeException(createStaticMessage("Feature %s not registered", feature));
      }
      return featureFlags.get(feature);
    }
  }
}
