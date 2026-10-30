/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.context;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static com.zernova.runtime.core.api.util.ClassUtils.memoize;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.TypedComponentIdentifier;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.component.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Spring implementation of {@link ConfigurationComponentLocator}.
 *
 * since 4.0
 */
public class SpringConfigurationComponentLocator implements ConfigurationComponentLocator {

  private final Function<String, Boolean> isTemplateLocationFunction;
  private final Map<String, Component> componentsMap = new HashMap<>();
  private final Set<ComponentLocation> componentLocations = new HashSet<>();

  public SpringConfigurationComponentLocator() {
    this.isTemplateLocationFunction = memoize(location -> false, new ConcurrentHashMap<>());
  }

  public SpringConfigurationComponentLocator(Function<String, Boolean> isTemplateComponentFunction) {
    this.isTemplateLocationFunction = memoize(isTemplateComponentFunction, new ConcurrentHashMap<>());
  }

  /**
   * Adds a new component to the locator.
   *
   * @param component the component to be added
   */
  public void addComponent(Component component) {
    this.componentsMap.put(component.getLocation().getLocation(), component);
  }

  /**
   * Adds a new {@link ComponentLocation} to the locator.
   * <p>
   * This method is used in addition to {@link #addComponent(Component)} when the parser knows a certain location exists but the
   * component in that location is not available (i.e.: is lazy)
   *
   * @param location the component to be added
   */
  public void addComponentLocation(ComponentLocation location) {
    this.componentLocations.add(location);
  }

  /**
   * Removes the components from the locator
   */
  public void removeComponents() {
    this.componentsMap.clear();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<Component> find(Location location) {
    if (location == null || isTemplateLocationFunction.apply(location.getGlobalName())) {
      return empty();
    }
    return ofNullable(componentsMap.get(location.toString()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Component> find(ComponentIdentifier componentIdentifier) {
    return componentsMap.values().stream()
        .filter(component -> {
          TypedComponentIdentifier identifier;
          try {
            identifier = component.getLocation().getComponentIdentifier();
          } catch (NoSuchElementException e) {
            return false;
          }
          return identifier.getIdentifier().equals(componentIdentifier);
        }).collect(toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ComponentLocation> findAllLocations() {
    return unmodifiableList(new ArrayList<>(componentLocations));
  }
}
