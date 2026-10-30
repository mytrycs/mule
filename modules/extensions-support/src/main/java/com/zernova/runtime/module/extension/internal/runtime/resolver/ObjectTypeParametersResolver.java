/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.DefaultObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.ObjectBuilder;

/**
 * Provides a way to resolve the parameter values of a given {@link ObjectType}
 *
 * @since 4.0
 */
public interface ObjectTypeParametersResolver {

  /**
   * Adds the required {@link ValueResolver}s for the object parameters into the builder
   *
   * @param objectType the {@link ObjectType} to introspect
   * @param builder    the {@link ObjectBuilder} being used to create the owner object of the parameters
   */
  void resolveParameters(ObjectType objectType, DefaultObjectBuilder builder);

  /**
   * Adds the required {@link ValueResolver}s for the object parameters groups into the builder
   *
   * @param objectType the {@link ObjectType} to introspect
   * @param builder    the {@link ObjectBuilder} being used to create the owner object of the parameters
   */
  void resolveParameterGroups(ObjectType objectType, DefaultObjectBuilder builder);
}
