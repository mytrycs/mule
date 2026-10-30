/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.property;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ModelProperty;

/**
 * {@link ModelProperty} to be used on any {@link com.zernova.runtime.api.meta.model.EnrichableModel} that has been added to an
 * {@link com.zernova.runtime.api.meta.model.ExtensionModel} in a certain {@link com.zernova.runtime.api.meta.ZernovaVersion} and it is not
 * desired to add it to versions of the extension model generated for older runtimes. If this {@link ModelProperty} is not
 * present, the model must not be filtered for any runtime version.
 *
 * @since 1.2.0
 */
public class SinceZernovaVersionModelProperty implements ModelProperty {

  public static final String NAME = "sinceMuleVersion";

  private final ZernovaVersion version;

  public SinceZernovaVersionModelProperty(String version) {
    this.version = new ZernovaVersion(version);
  }

  /**
   *
   * @return a {@link ZernovaVersion} from which the enriched model should appear in the
   *         {@link com.zernova.runtime.api.meta.model.ExtensionModel}
   */
  public ZernovaVersion getVersion() {
    return version;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isPublic() {
    return true;
  }
}


