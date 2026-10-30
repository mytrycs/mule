/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.data.sample;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.util.ClassUtils.instantiateClass;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.setValueIntoField;
import static com.zernova.sdk.api.data.sample.SampleDataException.CONNECTION_FAILURE;
import static com.zernova.sdk.api.data.sample.SampleDataException.MISSING_REQUIRED_PARAMETERS;
import static com.zernova.sdk.api.data.sample.SampleDataException.UNKNOWN;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.core.api.Injector;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.module.extension.api.runtime.resolver.ParameterValueResolver;
import com.zernova.runtime.module.extension.internal.loader.java.property.InjectableParameterInfo;
import com.zernova.runtime.module.extension.internal.loader.java.property.SampleDataProviderFactoryModelProperty;
import com.zernova.runtime.module.extension.internal.util.InjectableParameterResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.sdk.api.data.sample.SampleDataException;
import com.zernova.sdk.api.data.sample.SampleDataProvider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;

/**
 * Provides instances of {@link SampleDataProvider}
 *
 * @since 4.4.0
 */
public class SampleDataProviderFactory {

  private static final Logger LOGGER = getLogger(SampleDataProviderFactory.class);

  private final SampleDataProviderFactoryModelProperty factoryModelProperty;
  private final ParameterValueResolver parameterValueResolver;
  private final Supplier<Object> connectionSupplier;
  private final Supplier<Object> configurationSupplier;
  private final Field connectionField;
  private final Field configField;
  private final ReflectionCache reflectionCache;
  private final ExpressionManager expressionManager;
  private final Injector injector;
  private final ParameterizedModel parameterizedModel;

  public SampleDataProviderFactory(SampleDataProviderFactoryModelProperty factoryModelProperty,
                                   ParameterValueResolver parameterValueResolver,
                                   Supplier<Object> connectionSupplier,
                                   Supplier<Object> configurationSupplier,
                                   Field connectionField,
                                   Field configField,
                                   ReflectionCache reflectionCache,
                                   ExpressionManager expressionManager,
                                   Injector injector,
                                   ParameterizedModel parameterizedModel) {
    this.factoryModelProperty = factoryModelProperty;
    this.parameterValueResolver = parameterValueResolver;
    this.connectionSupplier = connectionSupplier;
    this.configurationSupplier = configurationSupplier;
    this.connectionField = connectionField;
    this.configField = configField;
    this.reflectionCache = reflectionCache;
    this.expressionManager = expressionManager;
    this.injector = injector;
    this.parameterizedModel = parameterizedModel;
  }

  <T, A> SampleDataProvider<T, A> createSampleDataProvider() throws SampleDataException {
    Class<? extends SampleDataProvider<T, A>> providerClass = factoryModelProperty.getSampleDataProviderClass();

    try {
      SampleDataProvider<T, A> resolver = instantiateClass(providerClass);
      initialiseIfNeeded(resolver, injector);

      InjectableParameterResolver injectableParameterResolver =
          new InjectableParameterResolver(parameterizedModel, parameterValueResolver, expressionManager,
                                          factoryModelProperty.getInjectableParameters());
      injectProviderFields(resolver, injectableParameterResolver);

      if (factoryModelProperty.usesConnection()) {
        Object connection;
        try {
          connection = connectionSupplier.get();
        } catch (Exception e) {
          throw new SampleDataException("Failed to establish connection: " + e.getMessage(), CONNECTION_FAILURE, e);
        }
        if (connection == null) {
          throw new SampleDataException("The sample data provider requires a connection and none was provided",
                                        MISSING_REQUIRED_PARAMETERS);
        }
        setValueIntoField(resolver, connection, connectionField);
      }

      if (factoryModelProperty.usesConfig()) {
        Object config = configurationSupplier.get();
        if (config == null) {
          throw new SampleDataException("The sample data provider requires a configuration and none was provided",
                                        MISSING_REQUIRED_PARAMETERS);
        }
        setValueIntoField(resolver, config, configField);
      }
      return resolver;
    } catch (SampleDataException e) {
      throw e;
    } catch (Exception e) {
      throw new SampleDataException("An error occurred trying to create a SampleDataProvider", UNKNOWN, e);
    }
  }

  private void injectProviderFields(SampleDataProvider resolver, InjectableParameterResolver resolvedParameters)
      throws SampleDataException {
    List<String> missingParameters = new ArrayList<>();
    for (InjectableParameterInfo injectableParam : factoryModelProperty.getInjectableParameters()) {
      String parameterName = injectableParam.getParameterName();
      Object parameterValue = resolvedParameters.getInjectableParameterValue(injectableParam.getParameterName());
      if (parameterValue != null) {
        setValueIntoField(resolver, parameterValue, parameterName, reflectionCache);
      } else if (injectableParam.isRequired()) {
        missingParameters.add(parameterName);
      }
    }

    if (!missingParameters.isEmpty()) {
      throw new SampleDataException("Unable to retrieve Sample Data. There are missing required parameters for the resolution: "
          + missingParameters, MISSING_REQUIRED_PARAMETERS);
    }
  }
}
