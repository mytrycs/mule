/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.application.wrapper;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.deployment.model.api.DeploymentException;
import com.zernova.runtime.deployment.model.api.DeploymentStopException;
import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.module.deployment.impl.internal.application.ApplicationWrapper;

import java.io.IOException;

/**
 * Adds a way to simulate errors on application deployment phases
 */
public class TestApplicationWrapper extends ApplicationWrapper {

  private boolean failOnStopApplication;

  private boolean failOnDisposeApplication;

  public TestApplicationWrapper(Application delegate) throws IOException {
    super(delegate);
  }

  @Override
  public void dispose() {
    if (failOnDisposeApplication) {
      throw new DeploymentException(createStaticMessage("Error disposing application"));
    }

    getDelegate().dispose();
  }

  @Override
  public void stop() {
    if (failOnStopApplication) {
      throw new DeploymentStopException(createStaticMessage("Error stopping application"));
    }

    getDelegate().stop();
  }

  public void setFailOnStopApplication(boolean failOnStopApplication) {
    this.failOnStopApplication = failOnStopApplication;
  }

  public void setFailOnDisposeApplication(boolean failOnDisposeApplication) {
    this.failOnDisposeApplication = failOnDisposeApplication;
  }
}
