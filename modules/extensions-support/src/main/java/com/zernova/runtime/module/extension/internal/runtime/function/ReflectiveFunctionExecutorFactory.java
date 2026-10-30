/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.function;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.metadata.DataType.fromType;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.getType;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.isTypedValue;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.toDataType;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.function.FunctionModel;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.FunctionParameter;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.runtime.function.FunctionExecutor;
import com.zernova.runtime.extension.api.runtime.function.FunctionExecutorFactory;
import com.zernova.runtime.extension.api.runtime.function.FunctionParameterDefaultValueResolverFactory;

import com.google.common.base.Defaults;

import java.lang.reflect.Method;
import java.util.List;

/**
 * An implementation of {@link FunctionExecutorFactory} which produces instances of {@link ReflectiveExpressionFunctionExecutor}.
 *
 * @param <T> the type of the class in which the implementing method is declared
 * @since 3.7.0
 */
public final class ReflectiveFunctionExecutorFactory<T> implements FunctionExecutorFactory {

  private final Class<T> implementationClass;
  private final Method method;

  public ReflectiveFunctionExecutorFactory(Class<T> implementationClass, Method method) {
    checkArgument(implementationClass != null, "implementationClass cannot be null");
    checkArgument(method != null, "operationMethod cannot be null");

    this.implementationClass = implementationClass;
    this.method = method;
  }

  @Override
  public FunctionExecutor createExecutor(FunctionModel functionModel,
                                         FunctionParameterDefaultValueResolverFactory defaultResolverFactory) {

    DataType returnType = fromType(getType(functionModel.getOutput().getType())
        .orElseThrow(() -> new ZernovaRuntimeException(createStaticMessage(format(
                                                                               "Failed to obtain the return type for function [%s]",
                                                                               functionModel.getName())))));

    List<FunctionParameter> functionParameters = functionModel.getAllParameterModels().stream().map(p -> {
      MetadataType paramType = p.getType();
      DataType type = isTypedValue(paramType) ? fromType(TypedValue.class) : toDataType(paramType);
      if (p.isRequired()) {
        return new FunctionParameter(p.getName(), type);
      }

      Object defaultValue = p.getDefaultValue();
      if (defaultValue == null) {
        return new FunctionParameter(p.getName(), type, context -> Defaults.defaultValue(type.getType()));
      }

      return new FunctionParameter(p.getName(), type, defaultResolverFactory.create(defaultValue, type));

    }).collect(toList());

    return new ReflectiveExpressionFunctionExecutor(functionModel, returnType, functionParameters, method, getDelegateInstance());
  }

  private Object getDelegateInstance() {
    Object delegate;
    try {
      delegate = implementationClass.newInstance();
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create instance of operation class "
          + implementationClass.getName()), e);
    }
    return delegate;
  }

}
