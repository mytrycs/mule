/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import static java.lang.String.format;
import static com.zernova.runtime.api.functional.Either.left;
import static com.zernova.runtime.api.functional.Either.right;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.checkInstantiable;

import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.extension.api.exception.IllegalSourceModelDefinitionException;
import com.zernova.runtime.extension.api.runtime.source.SdkSourceFactory;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.sdk.api.runtime.source.Source;

public final class DefaultSdkSourceFactory implements SdkSourceFactory {

  private final Class<?> sourceType;
  private final boolean isLegacySourceType;

  public DefaultSdkSourceFactory(Class<?> sourceType) {
    checkInstantiable(sourceType, new ReflectionCache());
    if (com.zernova.runtime.extension.api.runtime.source.Source.class.isAssignableFrom(sourceType)) {
      isLegacySourceType = true;
    } else if (Source.class.isAssignableFrom(sourceType)) {
      isLegacySourceType = false;
    } else {
      throw new IllegalSourceModelDefinitionException(format("Source type %s must extend either %s or %s classes",
                                                             sourceType.getName(),
                                                             Source.class.getName(),
                                                             com.zernova.runtime.extension.api.runtime.source.Source.class
                                                                 .getName()));
    }
    this.sourceType = sourceType;
  }

  @Override
  public Either<Source, com.zernova.runtime.extension.api.runtime.source.Source> createMessageSource() {
    try {
      Object source = ClassUtils.instantiateClass(sourceType);
      return isLegacySourceType ? right(Source.class, (com.zernova.runtime.extension.api.runtime.source.Source) source)
          : left((Source) source, com.zernova.runtime.extension.api.runtime.source.Source.class);
    } catch (Exception e) {
      throw new RuntimeException("Exception found trying to instantiate source type " + sourceType.getName(), e);
    }
  }
}
