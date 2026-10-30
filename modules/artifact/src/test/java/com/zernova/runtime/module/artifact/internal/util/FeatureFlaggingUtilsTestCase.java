/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.internal.util;

import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.tck.junit4.rule.SystemProperty;
import com.zernova.tck.size.SmallTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.module.artifact.internal.util.test.DeploymentTestingFeatures.ALWAYS_ON_FEATURE;
import static com.zernova.runtime.module.artifact.internal.util.test.DeploymentTestingFeatures.OVERRIDEABLE_FEATURE;
import static com.zernova.runtime.module.artifact.internal.util.test.DeploymentTestingFeatures.OVERRIDEABLE_FEATURE_OVERRIDE;
import static com.zernova.test.allure.AllureConstants.DeploymentConfiguration.FeatureFlaggingStory.FEATURE_FLAGGING;

@SmallTest
@Story(FEATURE_FLAGGING)
@Issue("MULE-19402")
public class FeatureFlaggingUtilsTestCase {

  private final ArtifactDescriptor artifactDescriptor = mock(ArtifactDescriptor.class);

  @Rule
  public SystemProperty systemProperty = new SystemProperty(OVERRIDEABLE_FEATURE_OVERRIDE, "true");

  @BeforeClass
  public static void registerTestingFeatures() {
    // Ensure that the testing feature flags are registered.
    ALWAYS_ON_FEATURE.getClass();
  }

  @Before
  public void before() {
    when(artifactDescriptor.getName()).thenReturn("test-artifact");
  }

  @Test
  @Issue("MULE-19402")
  public void testFeature() {
    assertThat(FeatureFlaggingUtils.isFeatureEnabled(ALWAYS_ON_FEATURE, artifactDescriptor), is(true));
  }

  @Test
  @Issue("MULE-19402")
  public void testOverriddenFeature() {
    assertThat(FeatureFlaggingUtils.isFeatureEnabled(OVERRIDEABLE_FEATURE, artifactDescriptor), is(true));
  }

}
