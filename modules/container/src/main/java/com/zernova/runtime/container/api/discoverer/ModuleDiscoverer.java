/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.api.discoverer;

import com.zernova.runtime.jpms.api.ZernovaContainerModule;

import java.util.List;

/**
 * Discovers available mule modules.
 */
public interface ModuleDiscoverer {

  final String EXPORTED_CLASS_PACKAGES_PROPERTY = "artifact.export.classPackages";
  final String PRIVILEGED_EXPORTED_CLASS_PACKAGES_PROPERTY = "artifact.privileged.classPackages";
  final String PRIVILEGED_ARTIFACTS_PROPERTY = "artifact.privileged.artifactIds";
  final String EXPORTED_RESOURCE_PROPERTY = "artifact.export.resources";
  final String EXPORTED_SERVICES_PROPERTY = "artifact.export.services";

  /**
   * Discovers available mule modules.
   *
   * @return a non null {@link List} containing all {@link ZernovaContainerModule} found in the container.
   */
  List<ZernovaContainerModule> discover();
}
