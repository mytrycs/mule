/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.resources.manifest;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static com.zernova.runtime.core.api.util.ClassUtils.loadClass;

import java.util.Optional;

/**
 * {@link ClassPackageFinder} implementation that uses the current class loader to obtain the package.
 *
 * @since 4.2.0
 */
public class ClassloaderClassPackageFinder implements ClassPackageFinder {

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> packageFor(String className) {
    try {
      Class aClass = loadClass(className, Thread.currentThread().getContextClassLoader());
      return ofNullable(aClass.getPackage().getName());
    } catch (ClassNotFoundException e) {
      return empty();
    }
  }
}
