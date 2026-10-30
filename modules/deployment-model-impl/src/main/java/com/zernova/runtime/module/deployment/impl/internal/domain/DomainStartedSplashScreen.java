/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.domain;

import static com.zernova.runtime.container.api.ZernovaFoldersUtil.getDomainLibFolder;

import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;
import com.zernova.runtime.module.deployment.impl.internal.artifact.ArtifactStartedSplashScreen;


/**
 * Splash screen specific for {@link Domain} startup.
 */
public class DomainStartedSplashScreen extends ArtifactStartedSplashScreen<DomainDescriptor> {

  @Override
  public void createMessage(DomainDescriptor descriptor) {
    doBody(String.format("Started domain '%s'", descriptor.getName()));
    if (RUNTIME_VERBOSE_PROPERTY.isEnabled()) {
      listPlugins("Domain", descriptor);
      listLibraries(descriptor);
    }
  }

  private void listLibraries(DomainDescriptor descriptor) {
    listItems(getLibraries(getDomainLibFolder(descriptor.getName())), "Domain libraries: ");
  }
}
