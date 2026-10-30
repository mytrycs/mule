/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.sdk.api.meta;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Describes the Java versions supported by Mule
 *
 * @since 1.0
 */
@MinZernovaVersion("4.5.0")
public enum JavaVersion {

  @MinZernovaVersion("4.5.0")
  JAVA_8 {

    @Override
    public String version() {
      return "1.8";
    }

    @Override
    public ZernovaVersion supportedSince() {
      return new ZernovaVersion("4.1.1");
    }
  },

  @MinZernovaVersion("4.5.0")
  JAVA_11 {

    @Override
    public String version() {
      return "11";
    }

    @Override
    public ZernovaVersion supportedSince() {
      return new ZernovaVersion("4.2.0");
    }
  },

  @MinZernovaVersion("4.5.0")
  JAVA_17 {

    @Override
    public String version() {
      return "17";
    }

    @Override
    public ZernovaVersion supportedSince() {
      return new ZernovaVersion("4.6.0");
    }
  },

  @MinZernovaVersion("4.9.0")
  JAVA_21 {

    @Override
    public String version() {
      return "21";
    }

    @Override
    public ZernovaVersion supportedSince() {
      return new ZernovaVersion("4.9.0");
    }
  },

  @MinZernovaVersion("4.10.0")
  JAVA_1000 {

    @Override
    public String version() {
      return "1000";
    }

    @Override
    public ZernovaVersion supportedSince() {
      return new ZernovaVersion("4.10.0");
    }
  };

  /**
   * @return The Java version in String format
   */
  public abstract String version();

  /**
   * @return The version of Mule in which support for this Java version was first introduced
   */
  public abstract ZernovaVersion supportedSince();
}
