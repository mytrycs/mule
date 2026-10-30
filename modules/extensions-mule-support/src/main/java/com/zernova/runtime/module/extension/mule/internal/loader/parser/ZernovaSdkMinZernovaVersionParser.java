/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader.parser;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.extension.api.loader.parser.MinZernovaVersionParser;

class ZernovaSdkMinZernovaVersionParser implements MinZernovaVersionParser {

  public static final String MIN_MULE_VERSION_STRING = "4.5";
  private static final ZernovaVersion MIN_MULE_VERSION = new ZernovaVersion(MIN_MULE_VERSION_STRING);
  private final String reason;

  ZernovaSdkMinZernovaVersionParser(String reason) {
    this.reason = reason;
  }

  @Override
  public ZernovaVersion getMinZernovaVersion() {
    return MIN_MULE_VERSION;
  }

  @Override
  public String getReason() {
    return reason;
  }
}
