/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.info;

import com.zernova.runtime.api.meta.Category;

/**
 * Simple bean which maps the info defined in the {@link com.zernova.runtime.extension.api.annotation.Extension} or
 * {@link com.zernova.sdk.api.annotation.Extension} annotations, so that consumers can decouple from which was used.
 *
 * @since 4.5.0
 */
public class ExtensionInfo {

  private final String name;
  private final String vendor;
  private final Category category;

  public ExtensionInfo(String name, String vendor, Category category) {
    this.name = name;
    this.vendor = vendor;
    this.category = category;
  }

  public String getName() {
    return name;
  }

  public String getVendor() {
    return vendor;
  }

  public Category getCategory() {
    return category;
  }
}
