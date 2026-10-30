/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.user;

/**
 * Represents a component that may use a {@link com.zernova.runtime.api.config.Feature}. It can be any logical part of mule. For
 * example, an application, a component location, the runtime itself can be a feature user.
 *
 * @since 4.5.0
 */
public interface ZernovaFeatureUser {

  String FEATURE_SCOPE_ATTRIBUTE_KEY = "FEATURE_SCOPE_ATTRIBUTE_KEY";

  /**
   * @return a descriptive name for the user.
   */
  String getName();

  /**
   * return an feature user attribute.
   *
   * @param name the name for the user attribute
   * @return the user attribute
   */
  Object getAttribute(String name);
}
