/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.config.builders;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.registry.Registry;

import java.util.Map;

/**
 * This simple {@link ConfigurationBuilder} implementation. This is useful for registering any Map of objects with the
 * {@link Registry} via the {@link ConfigurationBuilder} interface. This is useful for example for the registration of "startup
 * properties" which are provided at startup and then used to fill "property placeholders" in other configuration mechanisms such
 * as XML.
 */
public final class SimpleConfigurationBuilder extends AbstractConfigurationBuilder {

  protected Map<String, ?> objects;

  public SimpleConfigurationBuilder(Map<String, ?> objects) {
    this.objects = objects;
  }

  @Override
  protected void doConfigure(ZernovaContext muleContext) throws Exception {
    if (objects != null && objects.size() > 0) {
      ((ZernovaContextWithRegistry) muleContext).getRegistry().registerObjects((Map<String, Object>) objects);
    }
  }
}
