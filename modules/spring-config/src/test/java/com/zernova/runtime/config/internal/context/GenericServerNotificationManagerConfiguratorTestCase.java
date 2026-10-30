/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context;

import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_NOTIFICATION_MANAGER;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import com.zernova.runtime.config.utils.AppParserConfigurationBuilder;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import org.junit.Test;

public class GenericServerNotificationManagerConfiguratorTestCase extends AbstractZernovaContextTestCase {

  @Override
  protected ConfigurationBuilder getBuilder() throws Exception {
    return new AppParserConfigurationBuilder(new String[] {"./generic-server-notification-manager-test.xml"});
  }

  @Test
  public void testRegistryHasAGenericServerNotificationManagerIfNoDynamicConfigIsPresent() {
    assertThat(((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(OBJECT_NOTIFICATION_MANAGER), notNullValue());
  }

}
