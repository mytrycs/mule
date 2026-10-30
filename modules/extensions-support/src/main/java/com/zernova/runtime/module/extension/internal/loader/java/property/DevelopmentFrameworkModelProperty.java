/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.zernova.runtime.module.extension.internal.loader.java.property;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.extension.api.loader.ExtensionDevelopmentFramework;

/**
 * {@link ModelProperty} to indicate the {@link ExtensionDevelopmentFramework} used for developing the associated extension.
 * <p>
 * Note that some extensions may not have this property, meaning they were not developed by any of the main development
 * frameworks. An example of this could be extensions programmatically declared.
 *
 * @since 4.5, moved from extensions-api in 4.8
 */
public class DevelopmentFrameworkModelProperty implements ModelProperty {

  private static final long serialVersionUID = 1L;

  private final ExtensionDevelopmentFramework extensionDevelopmentFramework;

  public DevelopmentFrameworkModelProperty(ExtensionDevelopmentFramework extensionDevelopmentFramework) {
    this.extensionDevelopmentFramework = extensionDevelopmentFramework;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return "extensionDevelopmentFramework";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isPublic() {
    return false;
  }

  /**
   * @return The {@link ExtensionDevelopmentFramework} used for developing the associated extension.
   */
  public ExtensionDevelopmentFramework getDevelopmentFramework() {
    return extensionDevelopmentFramework;
  }
}
