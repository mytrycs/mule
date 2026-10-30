/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.type.runtime;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.module.extension.api.loader.java.type.ConnectionProviderElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ParameterizableTypeElement;

/**
 * {@link TypeWrapper} specification for classes that are considered as Connection Providers
 *
 * @since 4.0
 */
public class ConnectionProviderTypeWrapper<T> extends TypeWrapper
    implements ConnectionProviderElement, ParameterizableTypeElement {

  public ConnectionProviderTypeWrapper(Class<T> aClass, ClassTypeLoader classTypeLoader) {
    super(aClass, classTypeLoader);
  }
}
