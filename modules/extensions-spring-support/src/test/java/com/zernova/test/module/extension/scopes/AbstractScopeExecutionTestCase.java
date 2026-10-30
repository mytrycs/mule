/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.scopes;

import static java.lang.Runtime.getRuntime;

import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.scheduler.SchedulerConfig;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.tck.junit4.rule.SystemProperty;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import jakarta.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

public abstract class AbstractScopeExecutionTestCase extends AbstractExtensionFunctionalTestCase {

  protected static final String KILL_REASON = "I'm the one who knocks";

  @Inject
  protected SchedulerService schedulerService;

  protected Scheduler cpuLightScheduler;
  protected Scheduler testScheduler;

  @Rule
  public SystemProperty maxRedelivery = new SystemProperty("killingReason", KILL_REASON);

  @Override
  protected String getConfigFile() {
    return "scopes/heisenberg-scope-config.xml";
  }

  @Override
  protected boolean isDisposeContextPerClass() {
    return true;
  }

  @Before
  public void setUp() {
    cpuLightScheduler = schedulerService.cpuLightScheduler();
    testScheduler = schedulerService.customScheduler(SchedulerConfig.config().withName("SCOPE-TEST")
        .withMaxConcurrentTasks(2 + getRuntime().availableProcessors() * 2));
  }

  @After
  public void tearDown() {
    cpuLightScheduler.stop();
    testScheduler.stop();
  }
}
