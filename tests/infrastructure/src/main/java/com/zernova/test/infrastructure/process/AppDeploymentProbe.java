/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.infrastructure.process;

import com.zernova.tck.probe.Probe;

/**
 * Checks if a Mule application is successfully deployed.
 *
 * @deprecated use {@link com.zernova.test.infrastructure.process.rules.ZernovaDeployment#checkAppIsDeployed(String)} instead}
 */
@Deprecated
public class AppDeploymentProbe implements Probe {

  private boolean check;
  private ZernovaProcessController mule;
  private String appName;

  public static AppDeploymentProbe isDeployed(ZernovaProcessController mule, String appName) {
    return new AppDeploymentProbe(mule, appName, true);
  }

  public static AppDeploymentProbe notDeployed(ZernovaProcessController mule, String appName) {
    return new AppDeploymentProbe(mule, appName, false);
  }

  protected AppDeploymentProbe(ZernovaProcessController mule, String appName, Boolean check) {
    this.mule = mule;
    this.appName = appName;
    this.check = check;
  }

  public boolean isSatisfied() {
    return check == mule.isDeployed(appName);
  }

  public String describeFailure() {
    return "Application [" + appName + "] is " + (check ? "not" : "") + " deployed.";
  }
}
