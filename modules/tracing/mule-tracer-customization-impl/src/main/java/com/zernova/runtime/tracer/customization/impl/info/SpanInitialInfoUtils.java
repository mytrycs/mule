/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.customization.impl.info;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.location.ComponentLocation;

public class SpanInitialInfoUtils {

  private static final String CORE_EVENT_SPAN_NAME_SEPARATOR = ":";
  public static final String UNKNOWN = "unknown";

  /**
   * @param componentIdentifier the {@link ComponentIdentifier}
   * @return the name for the {@link com.zernova.runtime.api.profiling.tracing.Span}
   */
  public static String getSpanName(ComponentIdentifier componentIdentifier) {
    return getUnknownIfEmptyNamespace(componentIdentifier) + CORE_EVENT_SPAN_NAME_SEPARATOR
        + getUnknownIfEmptyName(componentIdentifier);
  }

  private static String getUnknownIfEmptyName(ComponentIdentifier componentIdentifier) {
    if (componentIdentifier == null) {
      return UNKNOWN;
    }

    return componentIdentifier.getName();
  }

  private static String getUnknownIfEmptyNamespace(ComponentIdentifier componentIdentifier) {
    if (componentIdentifier == null) {
      return UNKNOWN;
    }

    return componentIdentifier.getNamespace();
  }

  /**
   * @param componentLocation the component location
   * @return componentLocation as string or unknown
   */
  public static String getLocationAsString(ComponentLocation componentLocation) {
    if (componentLocation != null) {
      return componentLocation.getLocation();
    }

    return UNKNOWN;
  }

}
