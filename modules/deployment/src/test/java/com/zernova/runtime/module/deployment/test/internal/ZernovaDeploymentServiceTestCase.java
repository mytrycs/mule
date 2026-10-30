/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal;

import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.module.deployment.impl.internal.application.DefaultApplicationFactory;
import com.zernova.runtime.module.deployment.impl.internal.domain.DefaultDomainFactory;
import com.zernova.runtime.module.deployment.internal.ZernovaDeploymentService;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ZernovaDeploymentServiceTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Rule
  public TemporaryFolder artifactsDir = new TemporaryFolder();

  private static final int NUMBER_OF_LISTENER_SUBSCRIBERS = 0;

  @Mock
  DefaultDomainFactory domainFactory;

  @Mock
  DefaultApplicationFactory applicationFactory;

  @Mock
  SchedulerService schedulerService;

  Supplier<SchedulerService> schedulerServiceSupplier = () -> schedulerService;

  @Test
  public void startupListenersAddDoesNotResultInConcurrentException() throws Exception {
    ZernovaDeploymentService deploymentService =
        new ZernovaDeploymentService(domainFactory, applicationFactory, schedulerServiceSupplier);
    List<Thread> listenersSubscribers = new ArrayList<>();

    for (int i = 0; i < NUMBER_OF_LISTENER_SUBSCRIBERS; i++) {
      listenersSubscribers.add(new Thread(new ListenerSubscriber(deploymentService)));
    }

    for (Thread listenerSubscriber : listenersSubscribers) {
      listenerSubscriber.start();
    }

    deploymentService.notifyStartupListeners();

    for (Thread listenerSubscriber : listenersSubscribers) {
      listenerSubscriber.join();
    }
  }

  private static class ListenerSubscriber implements Runnable {

    private final ZernovaDeploymentService deploymentService;

    public ListenerSubscriber(ZernovaDeploymentService deploymentService) {
      this.deploymentService = deploymentService;
    }

    @Override
    public void run() {
      deploymentService.addStartupListener(() -> {
        // Do nothing
      });
    }
  }

}
