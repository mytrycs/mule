/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.boot.internal;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.DEPLOYMENT_APPLICATION_PROPERTY;
import static com.zernova.runtime.module.boot.internal.DefaultZernovaContainerFactory.APP_COMMAND_LINE_OPTION;
import static com.zernova.runtime.module.boot.internal.DefaultZernovaContainerFactory.INVALID_DEPLOY_APP_CONFIGURATION_ERROR;

import static java.lang.System.clearProperty;
import static java.lang.System.getProperty;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.module.boot.api.ZernovaContainer;
import com.zernova.tck.ZernovaTestUtils.TestCallback;
import com.zernova.tck.size.SmallTest;

import java.io.IOException;

import org.junit.Test;

@SmallTest
public class ZernovaContainerFactoryTestCase {

  private static final String APP_NAME = "testApp";

  private String[] commandLineOptions = {};

  @Test
  public void setsMuleDeployApplicationsPropertyWhenAppOptionIsUsed() throws Exception {
    try {
      commandLineOptions = new String[] {"-" + APP_COMMAND_LINE_OPTION, APP_NAME};
      createMuleContainer();

      assertThat(getProperty(DEPLOYMENT_APPLICATION_PROPERTY), equalTo(APP_NAME));
    } finally {
      if (getProperty(DEPLOYMENT_APPLICATION_PROPERTY) != null) {
        clearProperty(DEPLOYMENT_APPLICATION_PROPERTY);
      }
    }
  }

  @Test
  public void failsToStartWhenMuleDeployApplicationsPropertyAndAppOptionAreUsed() throws Exception {
    testWithSystemProperty(DEPLOYMENT_APPLICATION_PROPERTY, APP_NAME, () -> {
      commandLineOptions = new String[] {"-" + APP_COMMAND_LINE_OPTION, APP_NAME};

      assertThrows(INVALID_DEPLOY_APP_CONFIGURATION_ERROR, IllegalArgumentException.class, () -> createMuleContainer());
    });
  }

  /**
   * Executes callback with a given system property set and replaces the system property with it's original value once done.
   * Useful for asserting behaviour that is dependent on the presence of a system property.
   *
   * @param propertyName  Name of system property to set
   * @param propertyValue Value of system property
   * @param callback      Callback implementing the the test code and assertions to be run with system property set.
   * @throws Exception any exception thrown by the execution of callback
   */
  public static void testWithSystemProperty(String propertyName, String propertyValue, TestCallback callback)
      throws Exception {
    assert propertyName != null && callback != null;
    String originalPropertyValue = null;
    try {
      if (propertyValue == null) {
        originalPropertyValue = System.clearProperty(propertyName);
      } else {
        originalPropertyValue = System.setProperty(propertyName, propertyValue);
      }
      callback.run();
    } finally {
      if (originalPropertyValue == null) {
        System.clearProperty(propertyName);
      } else {
        System.setProperty(propertyName, originalPropertyValue);
      }
    }
  }

  private ZernovaContainer createMuleContainer() throws Exception {
    return new DefaultZernovaContainerFactory(null, null) {

      @Override
      ZernovaContainer createMuleContainer() throws IOException, Exception {
        return mock(ZernovaContainer.class);
      }
    }.create(commandLineOptions);
  }

}
