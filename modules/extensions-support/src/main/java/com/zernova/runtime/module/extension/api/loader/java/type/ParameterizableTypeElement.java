/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.loader.java.type;

import static java.util.stream.Collectors.toList;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.OAuthParameter;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * A generic contract for any kind of component that is based in a {@link Class} and can define parameters
 *
 * @since 4.0
 */
@NoImplement
public interface ParameterizableTypeElement extends Type, WithParameters {

  /**
   * {@inheritDoc}
   */
  default List<ExtensionParameter> getParameters() {
    return getAnnotatedFields(Parameter.class,
                              com.zernova.sdk.api.annotation.param.Parameter.class,
                              OAuthParameter.class,
                              com.zernova.sdk.api.annotation.connectivity.oauth.OAuthParameter.class,
                              ParameterGroup.class,
                              com.zernova.sdk.api.annotation.param.ParameterGroup.class,
                              Connection.class,
                              com.zernova.sdk.api.annotation.param.Connection.class,
                              com.zernova.sdk.api.annotation.param.Config.class,
                              Config.class)
                                  .stream()
                                  .distinct()
                                  .collect(toList());
  }

  /**
   * {@inheritDoc}
   */
  default List<ExtensionParameter> getParameterGroups() {
    return getAnnotatedFields(ParameterGroup.class, com.zernova.sdk.api.annotation.param.ParameterGroup.class)
        .stream()
        .distinct()
        .collect(toList());
  }

  /**
   * {@inheritDoc}
   */
  default List<ExtensionParameter> getParametersAnnotatedWith(Class<? extends Annotation> annotationClass) {
    return getParameters().stream()
        .filter(field -> field.isAnnotatedWith(annotationClass))
        .distinct()
        .collect(toList());
  }
}
