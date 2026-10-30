/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.maven;

import java.util.Set;

/**
 * Defines the attributes of an artifact. The set of packages and resources.
 */
class ArtifactAttributes {

  private Set<String> packages;
  private Set<String> resources;

  public ArtifactAttributes(Set<String> packages, Set<String> resources) {
    this.packages = packages;
    this.resources = resources;
  }

  public Set<String> getPackages() {
    return packages;
  }

  public Set<String> getResources() {
    return resources;
  }
}
