/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.utils;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.extension.api.loader.parser.MinZernovaVersionParser;

/**
 * Holds information about the minimum {@link ZernovaVersion} that a component can run on and the reason for that value.
 *
 * @since 4.5
 */
public class ResolvedMinZernovaVersion implements MinZernovaVersionParser {

  public static final ZernovaVersion FIRST_MULE_VERSION = new ZernovaVersion("4.1.1");
  private String componentName;
  private ZernovaVersion minZernovaVersion;
  private String reason;
  private ResolvedMinZernovaVersion innerComponent = null;

  public ResolvedMinZernovaVersion(String componentName, ZernovaVersion minZernovaVersion, String reason) {
    this.componentName = componentName;
    this.minZernovaVersion = minZernovaVersion;
    this.reason = reason;
  }

  public ZernovaVersion getMinZernovaVersion() {
    return minZernovaVersion;
  }

  public String getReason() {
    if (innerComponent != null) {
      return reason.concat(" ").concat(innerComponent.getReason());
    }
    return reason;
  }

  public String getName() {
    return componentName;
  }

  public void updateIfHigherMMV(ResolvedMinZernovaVersion candidate, String reason) {
    if (!(this.minZernovaVersion.atLeast(candidate.getMinZernovaVersion()))) {
      this.reason = reason;
      this.minZernovaVersion = candidate.getMinZernovaVersion();
      this.innerComponent = candidate;
    }
  }
}
