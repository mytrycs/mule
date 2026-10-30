/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.resolvers;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import com.zernova.runtime.config.internal.BeanWrapper;
import com.zernova.runtime.config.internal.registry.AbstractSpringRegistry;
import com.zernova.runtime.core.internal.lifecycle.InjectedDependenciesProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides custom dependencies declared if the target object implements {@link InjectedDependenciesProvider}
 */
public class DeclaredDependencyResolver {

  private AbstractSpringRegistry springRegistry;

  public DeclaredDependencyResolver(AbstractSpringRegistry springRegistry) {
    this.springRegistry = springRegistry;
  }

  public List<BeanWrapper> getDeclaredDependencies(Object object) {
    List<BeanWrapper> dependencies = new ArrayList<>();
    if (!(object instanceof InjectedDependenciesProvider)) {
      return dependencies;
    }
    dependencies = ((InjectedDependenciesProvider) object).getInjectedDependencies()
        .stream()
        .map(dependency -> dependency.reduce(this::beansOfTypeList, this::beansOfNameList))
        .flatMap(Collection::stream).collect(toList());
    return dependencies;
  }

  private List<BeanWrapper> beansOfNameList(String beanName) {
    return singletonList(new BeanWrapper(beanName, springRegistry.get(beanName)));
  }

  private List<BeanWrapper> beansOfTypeList(Class<?> type) {
    return Stream.of(springRegistry.getBeanNamesForType(type))
        .map(beanName -> new BeanWrapper(beanName, springRegistry.get(beanName)))
        .collect(toList());
  }
}
