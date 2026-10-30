/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.api.artifact;

import com.zernova.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import com.zernova.runtime.module.artifact.internal.util.ServiceRegistryDescriptorLoaderRepository;

/**
 * Factory to create instances of {@link DescriptorLoaderRepository}.
 *
 * @since 4.5
 */
public class DescriptorLoaderRepositoryFactory {

  /**
   * @return a newly created {@link DescriptorLoaderRepository} instance.
   */
  public DescriptorLoaderRepository createDescriptorLoaderRepository() {
    return new ServiceRegistryDescriptorLoaderRepository();
  }
}
