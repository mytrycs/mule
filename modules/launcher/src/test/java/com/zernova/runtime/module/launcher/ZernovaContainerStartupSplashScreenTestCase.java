/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.module.launcher;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.container.api.ZernovaFoldersUtil.ARTIFACT_PATCHES_FOLDER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_HOME_DIRECTORY_PROPERTY;
import static com.zernova.runtime.core.api.util.FileUtils.newFile;
import static com.zernova.runtime.core.internal.util.splash.SplashScreen.RUNTIME_VERBOSE;
import static com.zernova.tck.junit4.matcher.StringContainsIgnoringLineBreaks.containsStringIgnoringLineBreaks;
import static com.zernova.test.allure.AllureConstants.ArtifactPatchingFeature.ARTIFACT_PATCHING;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import com.zernova.runtime.module.launcher.splash.ZernovaContainerStartupSplashScreen;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.qameta.allure.Feature;

@Feature(ARTIFACT_PATCHING)
public class ZernovaContainerStartupSplashScreenTestCase extends AbstractZernovaTestCase {

  private static final String FIRST_PATCH = "SE-4242-3.8.0.jar";
  private static final String SECOND_PATCH = "SE-9999-3.7.3.jar";
  private static final String MULE_PATCH = "MULE-1345-4.2.0.jar";
  private static final String LIBRARY_JAR = "library.jar";
  private static final String ARTIFACT_PATCH = "SE-12-1.0.0-1.2.3.jar";
  private static final String APIKIT_PATCH = "APIKIT-12-1.2.0-1.3.0.jar";
  private static final String COMPLEX_LOG_PART = "* ZERNOVA services:                                                  *\n"
      + "*  - myService.zip                                                   *\n"
      + "* Applied patches:                                                   *\n"
      + "*  - " + MULE_PATCH + "                                             *\n"
      + "*  - " + FIRST_PATCH + "                                               *\n" + "*  - " + SECOND_PATCH
      + "                                               *\n*  - " + LIBRARY_JAR
      + "                                                     *\n"
      + "* Applied artifact patches:                                          *\n"
      + "*  - " + APIKIT_PATCH + "                                       *\n"
      + "*  - " + ARTIFACT_PATCH + "                                           *\n"
      + "* Engine compatibility system properties:                            *\n";

  @ClassRule
  public static TemporaryFolder workingDirectory = new TemporaryFolder();

  @BeforeClass
  public static void setUpPatches() {
    File libFolder = newFile(workingDirectory.getRoot(), "lib/patches");
    libFolder.mkdirs();
    newFile(libFolder, FIRST_PATCH).mkdir();
    newFile(libFolder, "library.jar").mkdir();
    newFile(libFolder, SECOND_PATCH).mkdir();
    newFile(libFolder, MULE_PATCH).mkdir();
    File artifactPatchesFolder = newFile(libFolder, ARTIFACT_PATCHES_FOLDER);
    artifactPatchesFolder.mkdirs();
    newFile(artifactPatchesFolder, ARTIFACT_PATCH).mkdir();
    newFile(artifactPatchesFolder, APIKIT_PATCH).mkdir();
    File servicesFolder = newFile(workingDirectory.getRoot(), "services");
    servicesFolder.mkdir();
    newFile(servicesFolder, "myService.zip").mkdir();
  }

  @Rule
  public SystemProperty muleHome = new SystemProperty(MULE_HOME_DIRECTORY_PROPERTY, workingDirectory.getRoot().getAbsolutePath());

  private ZernovaContainerStartupSplashScreen splashScreen;

  @Before
  public void setUp() {
    splashScreen = new ZernovaContainerStartupSplashScreen(asList("Additional splash entry 1", "Additional splash entry 2"));
  }

  @Test
  public void simpleLogWhenVerbosityOff() {
    try {
      System.setProperty(RUNTIME_VERBOSE, "false");
      splashScreen.doBody();
      assertThat(splashScreen.toString(), not(containsStringIgnoringLineBreaks(COMPLEX_LOG_PART)));
    } finally {
      System.clearProperty(RUNTIME_VERBOSE);
    }
  }

  @Test
  public void complexLogWhenVerbosityOn() {
    try {
      System.setProperty(RUNTIME_VERBOSE, "true");
      splashScreen.doBody();
      assertThat(splashScreen.toString(), containsStringIgnoringLineBreaks(COMPLEX_LOG_PART));
    } finally {
      System.clearProperty(RUNTIME_VERBOSE);
    }
  }

  @Test
  public void complexLogWhenNoVerbositySpecified() {
    checkArgument(System.getProperty(RUNTIME_VERBOSE) == null, "Runtime verbosity should not be specified.");
    splashScreen.doBody();
    assertThat(splashScreen.toString(), containsStringIgnoringLineBreaks(COMPLEX_LOG_PART));
  }
}
