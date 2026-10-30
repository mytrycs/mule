/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.troubleshooting.internal;

import com.zernova.runtime.module.troubleshooting.api.ArgumentDefinition;

public class DefaultArgumentDefinition implements ArgumentDefinition {

  private static final long serialVersionUID = -6414068782959732107L;

  private final String name;
  private final String description;
  private final boolean required;

  public DefaultArgumentDefinition(String name, String description, boolean required) {
    this.name = name;
    this.description = description;
    this.required = required;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean isRequired() {
    return required;
  }
}
