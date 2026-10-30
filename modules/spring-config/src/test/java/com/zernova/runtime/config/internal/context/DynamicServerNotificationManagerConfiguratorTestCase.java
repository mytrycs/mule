/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context;

import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_NOTIFICATION_MANAGER;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getExtensionModel;

import static java.util.Collections.singleton;

import static org.junit.Assert.assertSame;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.config.utils.AppParserConfigurationBuilder;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.util.Set;

import org.junit.Test;

public class DynamicServerNotificationManagerConfiguratorTestCase extends AbstractZernovaContextTestCase {

  @Override
  protected Set<ExtensionModel> getExtensionModels() {
    return singleton(getExtensionModel());
  }

  @Override
  protected ConfigurationBuilder getBuilder() throws Exception {
    return new AppParserConfigurationBuilder(new String[] {"./dynamic-server-notification-manager-test.xml"});
  }

  @Test
  public void testRegistryHasNoGenericServerNotificationManagerIfDynamicConfigIsPresent() {
    assertSame(((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObject(OBJECT_NOTIFICATION_MANAGER), null);
  }

}
