/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.application;

import static com.zernova.runtime.api.util.collection.SmallMap.of;

import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.core.internal.lifecycle.phases.NotInLifecyclePhase;
import com.zernova.runtime.deployment.model.api.application.ApplicationStatus;

import java.util.Map;

/**
 * Maps an application life cycle phase to an application status
 */
public class ApplicationStatusMapper {

  private static final Map<String, ApplicationStatus> statusMapping = getStatusMapping();

  public static ApplicationStatus getApplicationStatus(String currentPhase) {
    final ApplicationStatus applicationStatus = statusMapping.get(currentPhase);

    if (applicationStatus == null) {
      throw new IllegalStateException("Unknown lifecycle phase: " + currentPhase);
    }

    return applicationStatus;
  }

  private static Map<String, ApplicationStatus> getStatusMapping() {
    Map<String, ApplicationStatus> statusMapping = of(
                                                      NotInLifecyclePhase.PHASE_NAME, ApplicationStatus.CREATED,
                                                      Disposable.PHASE_NAME, ApplicationStatus.DESTROYED,
                                                      Stoppable.PHASE_NAME, ApplicationStatus.STOPPED,
                                                      Startable.PHASE_NAME, ApplicationStatus.STARTED,
                                                      Initialisable.PHASE_NAME, ApplicationStatus.INITIALISED);

    return statusMapping;
  }
}
