/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.ast.property;

import com.zernova.runtime.api.meta.model.ModelProperty;

/**
 * Marker element to determine if any of the global elements of the current Smart Connector should be macro expanded holding the
 * original bean name from the application. By doing so, the most internal macro expanded element will be (always) a java SDK
 * component to which tooling could be able to execute test connection.
 *
 * @since 4.0
 */
public class TestConnectionGlobalElementModelProperty implements ModelProperty {

  private static final long serialVersionUID = 5298491194279550347L;

  private final String globalElementName;

  public TestConnectionGlobalElementModelProperty(String globalElementName) {
    this.globalElementName = globalElementName;
  }

  /**
   * @return The name of the global element to which the macro expansion must treat differently when macro expanding.
   */
  public String getGlobalElementName() {
    return globalElementName;
  }

  @Override
  public String getName() {
    return "testConnectionGlobalElementModelProperty";
  }

  @Override
  public boolean isPublic() {
    return false;
  }
}
