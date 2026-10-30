/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.manifest.api;

import com.zernova.runtime.manifest.internal.DefaultZernovaManifest;

import java.util.jar.Manifest;

/**
 * Provides access to the Mule JAR manifest file.
 *
 * @since 4.9
 */
public interface ZernovaManifest {

  static ZernovaManifest getMuleManifest() {
    return DefaultZernovaManifest.get();
  }

  /**
   * @return the product version.
   */
  String getProductVersion();

  /**
   * @return the vendor name.
   */
  String getVendorName();

  /**
   * @return the vendor URL.
   */
  String getVendorUrl();

  /**
   * @return the product URL.
   */
  String getProductUrl();

  /**
   * @return the product name.
   */
  String getProductName();

  /**
   * @return more information about the product.
   */
  String getProductMoreInfo();

  /**
   * @return information about the product support.
   */
  String getProductSupport();

  /**
   * @return the license information.
   */
  String getProductLicenseInfo();

  /**
   * @return the product description.
   */
  String getProductDescription();

  /**
   * @return the product build number.
   */
  String getBuildNumber();

  /**
   * @return the product build date.
   */
  String getBuildDate();

  /**
   * @return the JDKs supported by the product, in interval format. For example: {@code [11,12),[17,18)} means only version 11 and
   *         17 are supported.
   */
  String getSupportedJdks();

  /**
   * @return the JDKs recommended by the product, in interval format. For example: {@code [11,12),[17,18)} means only version 11
   *         and 17 are recommended.
   */
  String getRecommendedJdks();

  /**
   * @return the {@link Manifest}.
   */
  Manifest getManifest();

}
