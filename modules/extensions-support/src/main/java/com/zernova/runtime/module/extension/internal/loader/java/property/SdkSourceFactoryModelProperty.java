/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.property;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.extension.api.runtime.source.SdkSourceFactory;
import com.zernova.runtime.extension.api.runtime.source.SourceFactory;

/**
 * A {@link ModelProperty} meant to be used on {@link SourceModel source models}, which provides access to a {@link SourceFactory}
 * used to create a message source
 *
 * @since 4.0
 */
public final class SdkSourceFactoryModelProperty implements ModelProperty {

  private final SdkSourceFactory sdkSourceFactory;

  /**
   * Creates a new instance
   *
   * @param sdkSourceFactory a {@link SourceFactory}
   */
  public SdkSourceFactoryModelProperty(SdkSourceFactory sdkSourceFactory) {
    this.sdkSourceFactory = sdkSourceFactory;
  }

  /**
   * @return a {@link SourceFactory}
   */
  public SdkSourceFactory getMessageSourceFactory() {
    return sdkSourceFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return "sdkSourceFactory";
  }

  /**
   * @return {@code false}
   */
  @Override
  public boolean isPublic() {
    return false;
  }
}
