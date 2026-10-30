/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.type.runtime;

import static com.zernova.runtime.api.util.collection.Collectors.toImmutableList;
import static com.zernova.runtime.module.extension.internal.loader.utils.ParameterUtils.getParameterFields;
import static com.zernova.runtime.module.extension.internal.loader.utils.ParameterUtils.getParameterGroupFields;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.module.extension.api.loader.java.type.ConfigurationElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.api.loader.java.type.FieldElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ParameterizableTypeElement;

import java.util.List;
import java.util.stream.Stream;

/**
 * {@link TypeWrapper} specification for classes that are considered as Configurations
 *
 * @since 4.0
 */
class ConfigurationWrapper extends ComponentWrapper implements ConfigurationElement, ParameterizableTypeElement {

  private final List<ExtensionParameter> parameters;

  ConfigurationWrapper(Class aClass, ClassTypeLoader typeLoader) {
    super(aClass, typeLoader);
    List<FieldElement> parameterFields = getParameterFields(this);
    List<FieldElement> parameterGroupFields = getParameterGroupFields(this);
    this.parameters = Stream.concat(parameterFields.stream(), parameterGroupFields.stream())
        .distinct()
        .collect(toImmutableList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExtensionParameter> getParameters() {
    return parameters;
  }
}
