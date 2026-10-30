/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.launcher.coreextension;

import com.zernova.runtime.container.api.ZernovaCoreExtension;

import java.util.Collection;
import java.util.List;

/**
 * Resolves dependencies between {@link ZernovaCoreExtension} instances.
 */
public interface ZernovaCoreExtensionDependencyResolver {

  /**
   * Resolves the dependencies between a given collection of {link ZernovaCoreExtension}
   *
   * @param coreExtensions core extensions to resolve
   * @return a list of core extensions ordered putting the independent extensions first and the most dependent ones last.
   * @throws UnresolveableDependencyException when dependencies cannot be successfully resolved
   */
  List<ZernovaCoreExtension> resolveDependencies(Collection<ZernovaCoreExtension> coreExtensions);
}
