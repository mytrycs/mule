/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.api.domain;

import java.util.Optional;
import java.util.Properties;

/**
 * Represents the description of a domain.
 *
 * @deprecated since 4.5. use com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor instead.
 */
@Deprecated
public class DomainDescriptor extends com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor {

  /**
   * Creates a new domain descriptor
   *
   * @param name domain name. Non empty.
   */
  public DomainDescriptor(String name) {
    super(name);
  }

  /**
   * Creates a new domain descriptor
   *
   * @param name                 domain name. Non empty.
   * @param deploymentProperties deploymentProperties
   */
  public DomainDescriptor(String name, Optional<Properties> deploymentProperties) {
    super(name, deploymentProperties);
  }

}
