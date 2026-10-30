/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.launcher.coreextension;

import com.zernova.runtime.container.api.CoreExtensionsAware;
import com.zernova.runtime.container.api.ZernovaCoreExtension;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.registry.IllegalDependencyInjectionException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Resolves dependencies using reflection to inject the required {@link ZernovaCoreExtension} in the dependant instance.
 */
public class ReflectionZernovaCoreExtensionDependencyResolver implements ZernovaCoreExtensionDependencyResolver {

  private final ZernovaCoreExtensionDependencyDiscoverer dependencyDiscoverer;

  public ReflectionZernovaCoreExtensionDependencyResolver() {
    this(new ReflectionZernovaCoreExtensionDependencyDiscoverer());
  }

  public ReflectionZernovaCoreExtensionDependencyResolver(ZernovaCoreExtensionDependencyDiscoverer dependencyDiscoverer) {
    this.dependencyDiscoverer = dependencyDiscoverer;
  }

  @Override
  public List<ZernovaCoreExtension> resolveDependencies(Collection<ZernovaCoreExtension> coreExtensions) {
    List<ZernovaCoreExtension> sortedCoreExtensions = new LinkedList<>(coreExtensions);
    sortCoreExtensionsByName(sortedCoreExtensions);

    List<ZernovaCoreExtension> coreExtensionAwareExtensions = findCoreExtensionAwareExtensions(sortedCoreExtensions);
    sortedCoreExtensions.removeAll(coreExtensionAwareExtensions);

    List<ZernovaCoreExtension> resolvedExtensions = resolveCoreExtensionDependenciesOrder(sortedCoreExtensions);
    resolvedExtensions.addAll(coreExtensionAwareExtensions);

    return resolvedExtensions;
  }

  private List<ZernovaCoreExtension> resolveCoreExtensionDependenciesOrder(List<ZernovaCoreExtension> sortedCoreExtensions) {
    List<ZernovaCoreExtension> unresolvedExtensions = new LinkedList<>(sortedCoreExtensions);
    List<ZernovaCoreExtension> resolvedExtensions = new LinkedList<>();

    boolean continueResolution = true;

    while (continueResolution) {
      int initialResolvedCount = resolvedExtensions.size();

      List<ZernovaCoreExtension> pendingUnresolvedExtensions = new LinkedList<>();

      for (ZernovaCoreExtension muleCoreExtension : unresolvedExtensions) {
        if (isResolvedCoreExtension(muleCoreExtension, resolvedExtensions)) {
          resolvedExtensions.add(muleCoreExtension);
        } else {
          pendingUnresolvedExtensions.add(muleCoreExtension);
        }
      }

      // Will try to resolve the extensions that are still unresolved
      unresolvedExtensions = pendingUnresolvedExtensions;

      continueResolution = resolvedExtensions.size() > initialResolvedCount;
    }

    if (unresolvedExtensions.size() != 0) {
      throw new UnresolveableDependencyException("Unable to resolve core extension dependencies: " + unresolvedExtensions);
    }

    return resolvedExtensions;
  }

  private boolean isResolvedCoreExtension(ZernovaCoreExtension muleCoreExtension, List<ZernovaCoreExtension> resolvedExtensions) {
    final List<LinkedZernovaCoreExtensionDependency> dependencies = dependencyDiscoverer.findDependencies(muleCoreExtension);

    boolean resolvedCoreExtension = dependencies.size() == 0;

    if (!resolvedCoreExtension && satisfiedDependencies(dependencies, resolvedExtensions)) {
      injectDependencies(muleCoreExtension, resolvedExtensions, dependencies);
      resolvedCoreExtension = true;
    }

    return resolvedCoreExtension;
  }

  private List<ZernovaCoreExtension> findCoreExtensionAwareExtensions(List<ZernovaCoreExtension> sortedCoreExtensions) {
    List<ZernovaCoreExtension> coreExtensionAwareExtensions = new LinkedList<>();

    for (ZernovaCoreExtension muleCoreExtension : sortedCoreExtensions) {
      if (muleCoreExtension instanceof CoreExtensionsAware) {
        final List<LinkedZernovaCoreExtensionDependency> dependencies = dependencyDiscoverer.findDependencies(muleCoreExtension);

        if (dependencies.isEmpty()) {

          coreExtensionAwareExtensions.add(muleCoreExtension);
        } else {
          throw new IllegalDependencyInjectionException("A class cannot implement CoreExtensionAware when is also using ZernovaCoreExtensionDependency");
        }
      }
    }
    return coreExtensionAwareExtensions;
  }

  private void sortCoreExtensionsByName(List<ZernovaCoreExtension> coreExtensionAwareExtensions1) {
    Collections.sort(coreExtensionAwareExtensions1, new Comparator<ZernovaCoreExtension>() {

      @Override
      public int compare(ZernovaCoreExtension coreExtension1, ZernovaCoreExtension coreExtension2) {
        return coreExtension1.getName().compareTo(coreExtension2.getName());
      }
    });
  }

  private void injectDependencies(ZernovaCoreExtension muleCoreExtension, List<ZernovaCoreExtension> resolvedExtensions,
                                  List<LinkedZernovaCoreExtensionDependency> dependencies) {
    for (LinkedZernovaCoreExtensionDependency linkedMuleCoreExtensionDependency : dependencies) {
      final ZernovaCoreExtension dependencyInstance =
          findDependencyInstance(resolvedExtensions, linkedMuleCoreExtensionDependency.getDependencyClass());

      try {
        linkedMuleCoreExtensionDependency.getDependantMethod().invoke(muleCoreExtension, new Object[] {dependencyInstance});
      } catch (Exception e) {
        throw new ZernovaRuntimeException(e);
      }
    }
  }

  private ZernovaCoreExtension findDependencyInstance(List<ZernovaCoreExtension> resolvedExtensions,
                                                   Class<? extends ZernovaCoreExtension> dependencyClass) {
    for (ZernovaCoreExtension coreExtension : resolvedExtensions) {
      if (dependencyClass.isAssignableFrom(coreExtension.getClass())) {
        return coreExtension;
      }
    }

    throw new IllegalArgumentException("Unable to find an instance for " + dependencyClass);
  }

  private boolean satisfiedDependencies(List<LinkedZernovaCoreExtensionDependency> dependencies,
                                        List<ZernovaCoreExtension> resolvedExtensions) {
    for (LinkedZernovaCoreExtensionDependency dependency : dependencies) {
      boolean isResolved = false;

      for (ZernovaCoreExtension resolved : resolvedExtensions) {
        if (dependency.getDependencyClass().isAssignableFrom(resolved.getClass())) {
          isResolved = true;
        }

      }

      if (!isResolved) {
        return false;
      }
    }

    return true;
  }
}
