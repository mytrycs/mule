/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.test.builders;

import com.zernova.runtime.api.config.custom.CustomizationService;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.internal.registry.Registry;

import java.util.Map;

/**
 * This is useful for registering any Map of objects with the {@link Registry} via the {@link ConfigurationBuilder} interface.
 *
 * @since 4.5
 */
public final class ServiceCustomizationsConfigurationBuilder extends AbstractConfigurationBuilder {

  private final Map<String, Object> objects;

  public ServiceCustomizationsConfigurationBuilder(Map<String, Object> objects) {
    this.objects = objects;
  }

  @Override
  protected void doConfigure(ZernovaContext muleContext) throws Exception {
    if (objects != null && objects.size() > 0) {
      CustomizationService customizationService = muleContext.getCustomizationService();
      objects.entrySet()
          .forEach(e -> customizationService.registerCustomServiceImpl(e.getKey(), e.getValue()));
    }
  }
}
