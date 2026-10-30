/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.utils;

import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.api.loader.java.type.FieldElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ParameterizableTypeElement;
import com.zernova.runtime.module.extension.api.loader.java.type.WithParameters;
import com.zernova.runtime.module.extension.internal.loader.java.type.runtime.ParameterizableTypeWrapper;

import java.util.List;

/**
 * Helper class for introspecting parameter types.
 *
 * @since 4.5
 */
public class ParameterUtils {

  private ParameterUtils() {}

  public static List<FieldElement> getConnectionFields(ParameterizableTypeWrapper parameterizableTypeWrapper) {
    return parameterizableTypeWrapper.getAnnotatedFields(Connection.class, com.zernova.sdk.api.annotation.param.Connection.class);
  }

  public static List<FieldElement> getConfigFields(ParameterizableTypeWrapper parameterizableTypeWrapper) {
    return parameterizableTypeWrapper.getAnnotatedFields(Config.class, com.zernova.sdk.api.annotation.param.Config.class);
  }

  public static List<FieldElement> getParameterFields(ParameterizableTypeElement parameterizableTypeElement) {
    List<FieldElement> parameterFields = parameterizableTypeElement.getAnnotatedFields(Parameter.class);
    parameterFields.addAll(parameterizableTypeElement.getAnnotatedFields(com.zernova.sdk.api.annotation.param.Parameter.class));
    return parameterFields;
  }

  public static List<FieldElement> getParameterGroupFields(ParameterizableTypeElement parameterizableTypeElement) {
    List<FieldElement> parameterFields = parameterizableTypeElement.getAnnotatedFields(ParameterGroup.class);
    parameterFields.addAll(parameterizableTypeElement.getAnnotatedFields(com.zernova.sdk.api.annotation.param.ParameterGroup.class));
    return parameterFields;
  }

  public static List<ExtensionParameter> getParameterGroups(WithParameters withParameters) {
    List<ExtensionParameter> parameterGroups = withParameters.getParametersAnnotatedWith(ParameterGroup.class);
    parameterGroups.addAll(withParameters.getParametersAnnotatedWith(com.zernova.sdk.api.annotation.param.ParameterGroup.class));
    return parameterGroups;
  }
}
