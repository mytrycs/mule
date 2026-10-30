/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor;

import static java.util.Collections.emptyMap;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.core.api.processor.Processor;

import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Marker interface that tells that a {@link Processor} is for internal use only.
 */
public interface InternalProcessor extends Component {

  @Override
  default Object getAnnotation(QName name) {
    return null;
  }

  @Override
  default Map<QName, Object> getAnnotations() {
    return emptyMap();
  }

  @Override
  default void setAnnotations(Map<QName, Object> annotations) {
    // Nothing to do
  }

  @Override
  default Location getRootContainerLocation() {
    return null;
  }

  @Override
  default ComponentLocation getLocation() {
    return null;
  }
}
