/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.core.api.config.TestingFeatures.TESTING_FEATURE;
import static com.zernova.runtime.core.api.config.TestingFeatures.TESTING_FEATURE_OVERRIDDEN_WITH_SYSTEM_PROPERTY;
import static com.zernova.test.allure.AllureConstants.DeploymentConfiguration.DEPLOYMENT_CONFIGURATION;
import static com.zernova.test.allure.AllureConstants.DeploymentConfiguration.FeatureFlaggingStory.FEATURE_FLAGGING;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import com.zernova.runtime.api.config.Feature;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.core.api.config.FeatureContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;
import com.zernova.tck.size.SmallTest;

@RunWith(Parameterized.class)
@io.qameta.allure.Feature(DEPLOYMENT_CONFIGURATION)
@Story(FEATURE_FLAGGING)
@SmallTest
public class FeatureFlaggingServiceBuilderTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Mock
  private ZernovaContext muleContext;

  @Mock
  private FeatureContext featureContext;

  @Mock
  private ZernovaConfiguration muleConfiguration;

  @Rule
  public SystemProperty systemProperty;

  private final Map<Feature, Predicate<ZernovaContext>> muleContextConfigs;
  private final Map<Feature, Predicate<FeatureContext>> featureContextConfigs;

  private final boolean expected;

  private final Feature feature;

  @Parameterized.Parameters(name = "Feature {0}: enabled={1} and SystemProperty={2} should be {3}")
  public static List<Object[]> parameters() {
    return asList(
                  new Object[] {TESTING_FEATURE, true, null, true},
                  new Object[] {TESTING_FEATURE, false, null, false},

                  new Object[] {TESTING_FEATURE_OVERRIDDEN_WITH_SYSTEM_PROPERTY, true, "false", false},
                  new Object[] {TESTING_FEATURE_OVERRIDDEN_WITH_SYSTEM_PROPERTY, true, "true", true},
                  new Object[] {TESTING_FEATURE_OVERRIDDEN_WITH_SYSTEM_PROPERTY, true, null, true},

                  new Object[] {TESTING_FEATURE_OVERRIDDEN_WITH_SYSTEM_PROPERTY, false, "false", false},
                  new Object[] {TESTING_FEATURE_OVERRIDDEN_WITH_SYSTEM_PROPERTY, false, "true", true},
                  new Object[] {TESTING_FEATURE_OVERRIDDEN_WITH_SYSTEM_PROPERTY, false, null, false});
  }

  @Before
  public void configureContext() {
    when(muleConfiguration.getId()).thenReturn("fake-id");
    when(muleContext.getConfiguration()).thenReturn(muleConfiguration);
    when(featureContext.getArtifactName()).thenReturn("fake-name");
  }

  public FeatureFlaggingServiceBuilderTestCase(Feature feature, boolean enabled, String systemPropertyValue, boolean expected) {
    muleContextConfigs = new HashMap<>();
    muleContextConfigs.put(feature, muleContext -> enabled);

    featureContextConfigs = new HashMap<>();
    featureContextConfigs.put(feature, featureContext -> enabled);

    this.feature = feature;
    this.expected = expected;

    if (systemPropertyValue != null) {
      feature.getOverridingSystemPropertyName().ifPresent(s -> this.systemProperty = new SystemProperty(s, systemPropertyValue));
    }
  }

  @Test
  @Issue("MULE-19402")
  public void testBuildUsingMuleContextConfigs() {
    FeatureFlaggingService featureFlaggingService = new FeatureFlaggingServiceBuilder()
        .withContext(muleContext)
        .withMuleContextFlags(muleContextConfigs)
        .build();
    assertThat(featureFlaggingService.isEnabled(feature), is(expected));
  }

  @Test
  @Issue("MULE-19402")
  public void testBuildUsingFeatureContextConfigs() {
    FeatureFlaggingService featureFlaggingService = new FeatureFlaggingServiceBuilder()
        .withContext(featureContext)
        .withFeatureContextFlags(featureContextConfigs)
        .build();
    assertThat(featureFlaggingService.isEnabled(feature), is(expected));
  }

}
