/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.manifest;

import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.test.allure.AllureConstants.ZernovaManifestFeature.MULE_MANIFEST;

import static java.lang.System.getProperty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.zernova.runtime.manifest.internal.DefaultZernovaManifest;

import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(MULE_MANIFEST)
public class ZernovaManifestTestCase {

  public static final String MULE_VERSION = "muleVersion";

  @Test
  public void getVersionFromManifestProperty() {
    String manifestVersion = getMuleManifest().getProductVersion();
    assertThat(manifestVersion, equalTo(getProperty(MULE_VERSION)));
  }

  @Test
  public void getVersionFromFallbackOption() {
    String fallbackVersion = ((DefaultZernovaManifest) getMuleManifest()).getProductVersionFromPropertiesFile();
    assertThat(fallbackVersion, equalTo(getProperty(MULE_VERSION)));
  }

}
