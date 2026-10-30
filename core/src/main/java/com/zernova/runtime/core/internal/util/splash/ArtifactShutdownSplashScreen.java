/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.splash;

import static com.zernova.runtime.core.api.config.i18n.CoreMessages.applicationWasUpForDuration;
import static com.zernova.runtime.core.api.config.i18n.CoreMessages.artifactShutdownNormally;

import com.zernova.runtime.core.api.ZernovaContext;

import java.util.Date;

public class ArtifactShutdownSplashScreen extends SplashScreen {

  @Override
  protected void doHeader(ZernovaContext context) {
    long currentTime = System.currentTimeMillis();
    header.add(artifactShutdownNormally(context.getArtifactType(), context.getConfiguration().getId(), new Date()).getMessage());
    long duration = 10;
    if (context.getStartDate() > 0) {
      duration = currentTime - context.getStartDate();
    }
    header.add(applicationWasUpForDuration(duration).getMessage());
  }
}


