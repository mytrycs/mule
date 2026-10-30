/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.internal;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import com.zernova.runtime.container.api.ZernovaModule;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Builds instances of {@link ZernovaModule}.
 * <p/>
 * Builder instances are not reusable. Creaet a new one every time a module must be created.
 */
public class TestModuleBuilder {

  private final String name;
  private Set<String> packages = new HashSet<>();
  private Set<String> resources = new HashSet<>();

  /**
   * Creates a new builder
   *
   * @param name name for the module. Not empty
   */
  public TestModuleBuilder(String name) {
    checkArgument(!StringUtils.isEmpty(name), "Name cannot be empty");
    this.name = name;
  }

  /**
   * Adds new java packages to be exported
   *
   * @param packages packages to export
   * @return {@code this}
   */
  public TestModuleBuilder exportingPackages(String... packages) {
    for (String packageName : packages) {
      this.packages.add(packageName);
    }

    return this;
  }

  /**
   * Adds new resource resources to be exported
   *
   * @param resources resources to export
   * @return {@code this}
   */
  public TestModuleBuilder exportingResources(String... resources) {
    for (String path : resources) {
      this.resources.add(path);
    }

    return this;
  }

  /**
   * Creates a module with the configured state
   *
   * @return a new {@link ZernovaModule} with the configured state.
   */
  public ZernovaModule build() {
    return new ZernovaModule(name, packages, resources, emptySet(), emptySet(), emptyList());
  }
}
