/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.junit4;

import static java.util.Collections.emptySet;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;

import com.zernova.runtime.api.config.custom.ServiceConfigurator;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.extension.ExtensionManager;

import java.util.Set;

/**
 * {@link ConfigurationBuilder} used to provide a mock implementation of {@link ExtensionManager} with an empty set of extensions.
 *
 * @since 4.2
 */
public class MockExtensionManagerConfigurationBuilder implements ConfigurationBuilder {

  private final Set<ExtensionModel> extensionModels;

  public MockExtensionManagerConfigurationBuilder(Set<ExtensionModel> extensionModels) {
    this.extensionModels = extensionModels;
  }

  public MockExtensionManagerConfigurationBuilder() {
    this(emptySet());
  }

  @Override
  public void addServiceConfigurator(ServiceConfigurator serviceConfigurator) {

  }

  @Override
  public void configure(ZernovaContext muleContext) {
    if (muleContext.getExtensionManager() == null) {
      withContextClassLoader(MockExtensionManagerConfigurationBuilder.class.getClassLoader(), () -> {
        ExtensionManager mockExtensionManager = mock(ExtensionManager.class, RETURNS_DEEP_STUBS);
        when(mockExtensionManager.getExtensions()).thenReturn(extensionModels);
        muleContext.setExtensionManager(mockExtensionManager);
      });
    }
  }
}
