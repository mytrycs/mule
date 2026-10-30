/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.domain;

import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;
import com.zernova.runtime.module.deployment.impl.internal.artifact.DeployableArtifactWrapper;

import java.io.IOException;

/**
 * Domain wrapper used to notify domain factory that a domain has been disposed or started.
 */
public class DomainWrapper extends DeployableArtifactWrapper<Domain, DomainDescriptor> implements Domain {

  private final DefaultDomainFactory domainFactory;

  protected DomainWrapper(final Domain delegate, final DefaultDomainFactory domainFactory) throws IOException {
    super(delegate);
    this.domainFactory = domainFactory;
  }

  @Override
  public boolean containsSharedResources() {
    return getDelegate().containsSharedResources();
  }

  @Override
  public void dispose() {
    try {
      getDelegate().dispose();
    } finally {
      domainFactory.dispose(this);
    }
  }
}
