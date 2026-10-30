/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.launcher.coreextension;

import com.zernova.runtime.container.api.ZernovaCoreExtension;

import java.lang.reflect.Method;

/**
 * Maps a core extension dependency class to a method in a dependant class
 */
public class LinkedZernovaCoreExtensionDependency {

  private final Class<? extends ZernovaCoreExtension> dependencyClass;

  private final Method dependantMethod;

  public LinkedZernovaCoreExtensionDependency(Class<? extends ZernovaCoreExtension> dependencyClass, Method dependantMethod) {
    this.dependencyClass = dependencyClass;
    this.dependantMethod = dependantMethod;
  }

  public Method getDependantMethod() {
    return dependantMethod;
  }

  public Class<? extends ZernovaCoreExtension> getDependencyClass() {
    return dependencyClass;
  }
}
