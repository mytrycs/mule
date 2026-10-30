/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.launcher.coreextension;

import com.zernova.runtime.container.api.ZernovaCoreExtension;
import com.zernova.runtime.container.api.ZernovaCoreExtensionDependency;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Discovers dependencies between {@link ZernovaCoreExtension} instances looking for methods annotated with
 * {@link ZernovaCoreExtensionDependency}
 */
public class ReflectionZernovaCoreExtensionDependencyDiscoverer implements ZernovaCoreExtensionDependencyDiscoverer {

  @Override
  public List<LinkedZernovaCoreExtensionDependency> findDependencies(ZernovaCoreExtension coreExtension) {
    List<LinkedZernovaCoreExtensionDependency> result = new LinkedList<LinkedZernovaCoreExtensionDependency>();

    final Method[] methods = coreExtension.getClass().getMethods();

    for (Method method : methods) {
      if (method.getAnnotation(ZernovaCoreExtensionDependency.class) != null) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
          if (ZernovaCoreExtension.class.isAssignableFrom(parameterTypes[0])) {
            final LinkedZernovaCoreExtensionDependency linkedMuleCoreExtensionDependency =
                new LinkedZernovaCoreExtensionDependency((Class<? extends ZernovaCoreExtension>) parameterTypes[0], method);
            result.add(linkedMuleCoreExtensionDependency);
          }
        }
      }
    }

    return result;
  }
}
