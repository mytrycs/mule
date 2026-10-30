/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.troubleshooting.internal.operations;

import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_ALERTING_SUPPORT;
import static com.zernova.runtime.module.troubleshooting.internal.TroubleshootingTestUtils.mockDeploymentService;
import static com.zernova.runtime.module.troubleshooting.internal.operations.EventDumpOperation.APPLICATION_ARGUMENT_NAME;
import static com.zernova.test.allure.AllureConstants.SupportabilityFeature.SUPPORTABILITY;
import static com.zernova.test.allure.AllureConstants.SupportabilityFeature.SupportabilityStory.ALERTS;

import static java.time.Instant.now;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Optional.of;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.alert.TimedDataAggregation;
import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.core.api.alert.ZernovaAlertingSupport;
import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.module.deployment.api.DeploymentService;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(SUPPORTABILITY)
@Story(ALERTS)
class AlertFuseboardOperationTestCase {

  private DeploymentService deploymentService;
  private AlertFuseboardOperation alertFuseboardOperation;

  @BeforeEach
  public void setup() {
    var alertsSummary = mockAlertsSummary();
    Application app1 = mockApplication("app1", alertsSummary);
    Application app2 = mockApplication("app2", emptyMap());
    deploymentService = mockDeploymentService(app1, app2);
    alertFuseboardOperation = new AlertFuseboardOperation(deploymentService);
  }

  private Map<String, TimedDataAggregation<Integer>> mockAlertsSummary() {
    var alerts = new TreeMap<String, TimedDataAggregation<Integer>>();

    alerts.put("ZZ_TOP_ALERT", new TimedDataAggregation<Integer>(now(), 99999, 9999, 999, 99));
    alerts.put("LNRD_SKNRD_ALERT", new TimedDataAggregation<Integer>(now(), 110, 10, 9, 9));

    return alerts;
  }

  @Test
  void whenNoApplicationIsPassedItReturnsAllApplications() throws IOException {
    final var writer = new StringWriter();
    alertFuseboardOperation.getCallback().execute(emptyMap(), writer);
    Object result = writer.toString();

    var expected = """
        Alert Fuseboard for application 'app1'
        --------------------------------------

          * LNRD_SKNRD_ALERT    110 /  10 /    9 /    9
          * ZZ_TOP_ALERT        99999 / 9999 /  999 /   99

        Alert Fuseboard for application 'app2'
        --------------------------------------

          No alerts triggered during the last hour.
        """;
    assertThat(result, is(equalTo(expected)));
  }

  @Test
  void whenApplicationIsPassedItReturnsOnlyThePassedOne() throws IOException {
    final var writer = new StringWriter();
    alertFuseboardOperation.getCallback().execute(singletonMap(APPLICATION_ARGUMENT_NAME, "app1"), writer);
    String result = writer.toString();

    var expected = """
          * LNRD_SKNRD_ALERT    110 /  10 /    9 /    9
          * ZZ_TOP_ALERT        99999 / 9999 /  999 /   99

        """;
    assertThat(result, is(equalTo(expected)));
  }

  private static Application mockApplication(String appName,
                                             Map<String, TimedDataAggregation<Integer>> alertsCountAggregation) {
    Application mockApp = mock(Application.class);
    when(mockApp.getArtifactName()).thenReturn(appName);

    ZernovaAlertingSupport muleAlertingSupport = mock(ZernovaAlertingSupport.class);
    when(muleAlertingSupport.alertsCountAggregation()).thenReturn(alertsCountAggregation);

    Registry registry = mock(Registry.class);
    when(registry.lookupByName(OBJECT_ALERTING_SUPPORT)).thenReturn(of(muleAlertingSupport));
    when(registry.lookupByType(ZernovaAlertingSupport.class)).thenReturn(of(muleAlertingSupport));

    ArtifactContext artifactContext = mock(ArtifactContext.class);
    when(artifactContext.getRegistry()).thenReturn(registry);
    when(mockApp.getArtifactContext()).thenReturn(artifactContext);
    return mockApp;
  }

}
