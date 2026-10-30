/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.source;

import static java.lang.String.format;

import com.zernova.runtime.extension.api.runtime.source.BackPressureMode;
import com.zernova.sdk.api.annotation.source.SourceClusterSupport;

/**
 * Utils class for java source related parsing.
 *
 * @since 4.5.0
 */
public class JavaSourceModelParserUtils {

  private JavaSourceModelParserUtils() {}

  public static BackPressureMode fromSdkBackPressureMode(com.zernova.sdk.api.runtime.source.BackPressureMode backPressureMode) {
    switch (backPressureMode) {
      case DROP:
        return BackPressureMode.DROP;
      case FAIL:
        return BackPressureMode.FAIL;
      case WAIT:
      default:
        return BackPressureMode.WAIT;
    }
  }

  public static SourceClusterSupport fromLegacySourceClusterSupport(com.zernova.runtime.extension.api.annotation.source.SourceClusterSupport legacyClusterSupport) {
    switch (legacyClusterSupport) {
      case NOT_SUPPORTED:
        return SourceClusterSupport.NOT_SUPPORTED;
      case DEFAULT_ALL_NODES:
        return SourceClusterSupport.DEFAULT_ALL_NODES;
      case DEFAULT_PRIMARY_NODE_ONLY:
        return SourceClusterSupport.DEFAULT_PRIMARY_NODE_ONLY;
    }
    throw new IllegalArgumentException(format("Unexpected cluster support mode %s was given.", legacyClusterSupport.name()));
  }
}
