/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.value;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.util.ClassUtils.instantiateClass;
import static com.zernova.runtime.extension.api.values.ValueResolvingException.MISSING_REQUIRED_PARAMETERS;
import static com.zernova.runtime.extension.api.values.ValueResolvingException.UNKNOWN;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.setValueIntoField;
import static com.zernova.sdk.api.data.sample.SampleDataException.CONNECTION_FAILURE;

import static java.lang.String.format;

import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.module.extension.api.runtime.resolver.ParameterValueResolver;
import com.zernova.runtime.module.extension.internal.loader.java.property.InjectableParameterInfo;
import com.zernova.runtime.module.extension.internal.loader.java.property.ValueProviderFactoryModelProperty;
import com.zernova.runtime.module.extension.internal.util.InjectableParameterResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.sdk.api.values.ValueProvider;
import com.zernova.sdk.api.values.ValueResolvingException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Provides instances of the {@link ValueProvider}
 *
 * @since 4.0
 */
public class ValueProviderFactory {

  private final ValueProviderFactoryModelProperty factoryModelProperty;
  private final ParameterValueResolver parameterValueResolver;
  private final Supplier<Object> connectionSupplier;
  private final Supplier<Object> configurationSupplier;
  private final Field connectionField;
  private final Field configField;
  private final ReflectionCache reflectionCache;
  private final ZernovaContext muleContext;
  private ExpressionManager expressionManager;
  private ParameterizedModel parameterizedModel;

  public ValueProviderFactory(ValueProviderFactoryModelProperty factoryModelProperty,
                              ParameterValueResolver parameterValueResolver, Supplier<Object> connectionSupplier,
                              Supplier<Object> configurationSupplier, Field connectionField, Field configField,
                              ReflectionCache reflectionCache, ZernovaContext muleContext,
                              ParameterizedModel parameterizedModel) {
    this.factoryModelProperty = factoryModelProperty;
    this.parameterValueResolver = parameterValueResolver;
    this.connectionSupplier = connectionSupplier;
    this.configurationSupplier = configurationSupplier;
    this.connectionField = connectionField;
    this.configField = configField;
    this.reflectionCache = reflectionCache;
    this.muleContext = muleContext;
    this.expressionManager = muleContext.getExpressionManager();
    this.parameterizedModel = parameterizedModel;
  }

  ValueProvider createValueProvider() throws ValueResolvingException {
    Class<?> resolverClass = factoryModelProperty.getValueProvider();

    try {
      Object resolver = instantiateClass(resolverClass);
      initialiseIfNeeded(resolver, true, muleContext);

      InjectableParameterResolver injectableParameterResolver =
          new InjectableParameterResolver(parameterizedModel, parameterValueResolver, expressionManager,
                                          factoryModelProperty.getInjectableParameters());

      injectValueProviderFields(resolver, injectableParameterResolver);

      if (factoryModelProperty.usesConnection()) {
        Object connection;
        try {
          connection = connectionSupplier.get();
        } catch (Exception e) {
          throw new ValueResolvingException("Failed to establish connection: " + e.getMessage(), CONNECTION_FAILURE, e);
        }

        if (connection == null) {
          throw new ValueResolvingException("The value provider requires a connection and none was provided",
                                            MISSING_REQUIRED_PARAMETERS);
        }
        setValueIntoField(resolver, connection, connectionField);
      }

      if (factoryModelProperty.usesConfig()) {
        Object config = configurationSupplier.get();
        if (config == null) {
          throw new ValueResolvingException("The value provider requires a configuration and none was provided",
                                            MISSING_REQUIRED_PARAMETERS);
        }
        setValueIntoField(resolver, config, configField);
      }
      return adaptResolver(resolver);
    } catch (ValueResolvingException e) {
      throw e;
    } catch (Exception e) {
      throw new ValueResolvingException("An error occurred trying to create a ValueProvider", UNKNOWN, e);
    }
  }

  private void injectValueProviderFields(Object resolver, InjectableParameterResolver resolvedParameters)
      throws ValueResolvingException {
    List<String> missingParameters = new ArrayList<>();
    for (InjectableParameterInfo injectableParam : factoryModelProperty.getInjectableParameters()) {
      Object parameterValue = resolvedParameters.getInjectableParameterValue(injectableParam.getParameterName());
      if (parameterValue != null) {
        setValueIntoField(resolver, parameterValue, injectableParam.getParameterName(), reflectionCache);
      } else if (injectableParam.isRequired()) {
        if (injectableParam.getParameterName().equals(injectableParam.getExtractionExpression())) {
          missingParameters.add(injectableParam.getExtractionExpression());
        } else {
          missingParameters
              .add(injectableParam.getParameterName() + "(taken from: " + injectableParam.getExtractionExpression() + ")");
        }
      }
    }

    if (!missingParameters.isEmpty()) {
      throw new ValueResolvingException("Unable to retrieve values. There are missing required parameters for the resolution: "
          + missingParameters, MISSING_REQUIRED_PARAMETERS);
    }
  }

  private ValueProvider adaptResolver(Object resolverObject) throws ValueResolvingException {
    if (resolverObject instanceof ValueProvider) {
      return (ValueProvider) resolverObject;
    } else if (resolverObject instanceof com.zernova.runtime.extension.api.values.ValueProvider) {
      return new SdkValueProviderAdapter((com.zernova.runtime.extension.api.values.ValueProvider) resolverObject);
    } else {
      throw new ValueResolvingException(format("An error occurred trying to create a ValueProvider: %s should implement %s or %s",
                                               resolverObject.getClass().getName(),
                                               ValueProvider.class.getName(),
                                               com.zernova.sdk.api.values.ValueProvider.class.getName()),
                                        UNKNOWN);
    }
  }

}
