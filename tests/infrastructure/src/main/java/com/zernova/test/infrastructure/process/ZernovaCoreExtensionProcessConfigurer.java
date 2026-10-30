/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.infrastructure.process;

import com.zernova.runtime.container.api.ZernovaCoreExtension;

public class ZernovaCoreExtensionProcessConfigurer implements ProcessBuilderConfigurer {

  private final Class<? extends ZernovaCoreExtension>[] coreExtensionClasses;

  public ZernovaCoreExtensionProcessConfigurer(Class<? extends ZernovaCoreExtension>... coreExtensionClasses) {
    if (coreExtensionClasses == null || coreExtensionClasses.length == 0) {
      throw new IllegalArgumentException("Could not create core extension configurer without core extensions");
    }
    this.coreExtensionClasses = coreExtensionClasses;
  }

  @Override
  public void configure(String instanceId, ConfigurableProcessBuilder configurableProcessBuilder) {
    StringBuilder propertyValue = new StringBuilder();
    for (Class<? extends ZernovaCoreExtension> coreExtensionClass : coreExtensionClasses) {
      propertyValue.append(coreExtensionClass.getName() + ",");
    }
    configurableProcessBuilder.addConfigurationAttribute(ZernovaContextProcessBuilder.MULE_CORE_EXTENSIONS_PROPERTY,
                                                         propertyValue.substring(0, propertyValue.length() - 1));
  }
}
