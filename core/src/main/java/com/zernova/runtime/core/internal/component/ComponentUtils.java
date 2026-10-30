/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.component;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.construct.FlowConstruct;

import java.util.Optional;

/**
 * Utility methods for components within the runtime.
 *
 * 4.0
 */
public interface ComponentUtils {

  /**
   * Returns the {@link FlowConstruct} of the root container element if there is one. Otherwise return {@link Optional#empty()}
   *
   * @param componentLocator the locator for the {@link FlowConstruct}
   * @param component        the component that may be configured within a {@link FlowConstruct}
   * @return the {@link FlowConstruct} of the root container element if there is one. Otherwise return {@link Optional#empty()}
   */
  static Optional<FlowConstruct> getFromAnnotatedObject(ConfigurationComponentLocator componentLocator,
                                                        Component component) {
    Optional<Component> objectFoundOptional = componentLocator.find(component.getRootContainerLocation());
    Optional<FlowConstruct> flowConstruct = objectFoundOptional.flatMap(objectFound -> objectFound instanceof FlowConstruct
        ? of((FlowConstruct) objectFound)
        : empty()).filter(object -> object != null);

    return flowConstruct;
  }

  /**
   * Returns the {@link FlowConstruct} of the root container element. If the root container is not a {@link FlowConstruct} then it
   * fails.
   *
   * @param componentLocator the locator for the {@link FlowConstruct}
   * @param component        the component that may be configured within a {@link FlowConstruct}
   * @return the {@link FlowConstruct} of the root container element. If the root container is not a {@link FlowConstruct} then it
   *         fails.
   */
  static FlowConstruct getFromAnnotatedObjectOrFail(ConfigurationComponentLocator componentLocator,
                                                    Component component) {
    return getFromAnnotatedObject(componentLocator, component)
        .orElseThrow(() -> new ZernovaRuntimeException(createStaticMessage(format(
                                                                               "Couldn't find FlowConstruct with global name %s or it was not an instance of FlowConstruct",
                                                                               component.getRootContainerLocation()
                                                                                   .toString()))));
  }

}
