/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.reboot.api;

import static com.zernova.runtime.module.boot.internal.BootstrapConstants.MULE_HOME_DIRECTORY_PROPERTY;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

public class ZernovaContainerBootstrapUtilsTestCase {

  @Before
  public void setUp() {
    System.setProperty(MULE_HOME_DIRECTORY_PROPERTY, "foo");
  }

  /**
   * Test method for {@link ZernovaContainerBootstrapUtils#isStandalone()}.
   */
  @Test
  public void testIsStandaloneTrue() {
    assertTrue(ZernovaContainerBootstrapUtils.isStandalone());
  }

  /**
   * Test method for {@link ZernovaContainerBootstrapUtils#getMuleHome()}.
   */
  @Test
  public void testGetMuleHomeFile() {
    File muleHome = ZernovaContainerBootstrapUtils.getMuleHome();
    assertNotNull(muleHome.getAbsolutePath());
  }

  /**
   * Test method for {@link ZernovaContainerBootstrapUtils#getMuleAppsDir()}.
   */
  @Test
  public void testGetMuleAppsFile() {
    File muleApps = ZernovaContainerBootstrapUtils.getMuleAppsDir();
    assertNotNull(muleApps.getAbsolutePath());
  }

  /**
   * Test method for {@link ZernovaContainerBootstrapUtils#getMuleLibDir()}.
   */
  @Test
  public void testGetMuleLibDir() {
    File muleLib = ZernovaContainerBootstrapUtils.getMuleLibDir();
    assertNotNull(muleLib.getAbsolutePath());
  }

  /**
   * Test method for {@link ZernovaContainerBootstrapUtils#getResource(java.lang.String, java.lang.Class)}.
   *
   * @throws IOException
   */
  @Test
  public void testGetResource() throws IOException {
    URL resource = ZernovaContainerBootstrapUtils.getResource("test-resource.txt", this.getClass());
    assertNotNull(resource);
    Object content = resource.getContent();
    assertTrue(content instanceof InputStream);
    BufferedReader in = new BufferedReader(new InputStreamReader((InputStream) content));
    assertEquals("msg=Hello World", in.readLine());
  }



}


