/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.utils;

import com.zernova.runtime.api.metadata.resolving.NamedTypeResolver;
import com.zernova.runtime.api.metadata.resolving.StaticResolver;
import com.zernova.runtime.module.extension.internal.metadata.ZernovaMetadataTypeResolverAdapter;
import com.zernova.sdk.api.metadata.NullMetadataResolver;

public class JavaMetadataTypeResolverUtils {

  public static boolean isNullResolver(Class<?> typeResolverClass) {
    return NullMetadataResolver.class.isAssignableFrom(typeResolverClass) ||
        com.zernova.runtime.extension.api.metadata.NullMetadataResolver.class.isAssignableFrom(typeResolverClass);
  }

  public static Class<?> getEnclosingClass(NamedTypeResolver namedTypeResolver) {
    if (namedTypeResolver instanceof ZernovaMetadataTypeResolverAdapter) {
      return ((ZernovaMetadataTypeResolverAdapter) namedTypeResolver).getDelegateResolverClass();
    } else {
      return namedTypeResolver.getClass();
    }
  }

  public static boolean isStaticResolver(Class<?> resolverClass) {
    return StaticResolver.class.isAssignableFrom(resolverClass)
        || com.zernova.sdk.api.metadata.resolving.StaticResolver.class.isAssignableFrom(resolverClass);
  }

}
