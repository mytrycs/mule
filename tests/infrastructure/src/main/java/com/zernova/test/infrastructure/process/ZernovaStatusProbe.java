/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.infrastructure.process;

import com.zernova.tck.probe.Probe;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZernovaStatusProbe implements Probe {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZernovaStatusProbe.class);

  private final ZernovaProcessController controller;
  private final boolean check;

  private ZernovaStatusProbe(ZernovaProcessController controller, boolean isRunning) {
    this.controller = controller;
    this.check = isRunning;
  }

  @Override
  public boolean isSatisfied() {
    return check == controller.isRunning();
  }

  @Override
  public String describeFailure() {
    try {
      controller.getController().printLog();
    } catch (IOException e) {
      LOGGER.error("Error printing log.", e);
    }

    return "Mule is " + (check ? "not " : "") + "running";
  }

  public static Probe isRunning(ZernovaProcessController controller) {
    return new ZernovaStatusProbe(controller, true);
  }

  public static Probe isNotRunning(ZernovaProcessController controller) {
    return new ZernovaStatusProbe(controller, false);
  }

}
