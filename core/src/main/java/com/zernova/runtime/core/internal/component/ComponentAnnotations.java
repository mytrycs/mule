/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.component;

import static com.zernova.runtime.api.component.AbstractComponent.ROOT_CONTAINER_NAME_KEY;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.component.Component;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * This interface holds the keys used internally by the runtime for the annotations added to the {@link Component}.
 */
public interface ComponentAnnotations {

  QName ANNOTATION_NAME = AbstractComponent.ANNOTATION_NAME;
  QName ANNOTATION_PARAMETERS = new QName("config", "componentParameters");
  QName ANNOTATION_COMPONENT_CONFIG = new QName("config", "componentConfiguration");

  /**
   * Updates the {@link Component} root container name.
   *
   * @param rootContainerName the root container name of the object.
   * @param component         the {@link Component} to update.
   */
  static void updateRootContainerName(String rootContainerName, Component component) {
    Map<QName, Object> previousAnnotations = new HashMap<>(component.getAnnotations());
    previousAnnotations.put(ROOT_CONTAINER_NAME_KEY, rootContainerName);
    component.setAnnotations(previousAnnotations);
  }

}
