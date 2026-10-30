/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.declaration.type.annotation;

/**
 * Pojo used to store a baseType when specified from {@code TypeDsl}. The format expected is prefix:element and they should both
 * always be specified.
 *
 * @since 1.0
 */
public class DslBaseType extends PrefixDefinedAnnotation {

  public DslBaseType() {
    super();
  }

  public DslBaseType(String prefix, String type) {
    super(prefix, type);
  }

  public DslBaseType(String formattedBaseType) throws IllegalArgumentException {
    super(formattedBaseType);
  }

  public String getType() {
    return getContent();
  }
}
