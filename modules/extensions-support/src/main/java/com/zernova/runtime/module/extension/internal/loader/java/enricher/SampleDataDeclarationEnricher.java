/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.enricher;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static com.zernova.runtime.module.extension.internal.data.sample.SampleDataUtils.getSampleDataProviderId;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getImplementingName;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.data.sample.SampleDataProviderModel;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExecutableComponentDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.SourceDeclaration;
import com.zernova.runtime.api.meta.model.parameter.ActingParameterModel;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.declaration.type.DefaultExtensionsTypeLoaderFactory;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.IdempotentDeclarationEnricherWalkDelegate;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;
import com.zernova.runtime.extension.api.model.parameter.ImmutableActingParameterModel;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.api.loader.java.type.FieldElement;
import com.zernova.runtime.module.extension.internal.loader.java.property.ImplementingMethodModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.ImplementingTypeModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.SampleDataProviderFactoryModelProperty.SampleDataProviderFactoryModelPropertyBuilder;
import com.zernova.runtime.module.extension.internal.loader.java.type.runtime.ParameterizableTypeWrapper;
import com.zernova.sdk.api.annotation.binding.Binding;
import com.zernova.sdk.api.annotation.data.sample.SampleData;
import com.zernova.sdk.api.data.sample.SampleDataProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link DeclarationEnricher} implementation that walks through a {@link ExtensionDeclaration} and looks for sources and
 * operations annotated with {@link SampleData}.
 *
 * Detected components are enriched with a {@link SampleDataProviderModel} and the necessary {@link ModelProperty} for the
 * component being executed.
 *
 * @since 4.4.0
 */
public class SampleDataDeclarationEnricher implements WalkingDeclarationEnricher {

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    return of(new IdempotentDeclarationEnricherWalkDelegate() {

      @Override
      public void onSource(SourceDeclaration declaration) {
        declaration.getModelProperty(ImplementingTypeModelProperty.class).ifPresent(property -> {
          SampleData annotation = property.getType().getAnnotation(SampleData.class);
          if (annotation != null) {
            declaration.setSampleDataProviderModel(createSampleDataModel(annotation, declaration));
          }
        });
      }

      @Override
      public void onOperation(OperationDeclaration declaration) {
        declaration.getModelProperty(ImplementingMethodModelProperty.class).ifPresent(property -> {
          Method method = property.getMethod();
          SampleData annotation = method.getAnnotation(SampleData.class);
          if (annotation != null) {
            declaration.setSampleDataProviderModel(createSampleDataModel(annotation, declaration));
          }
        });
      }
    });
  }

  private SampleDataProviderModel createSampleDataModel(SampleData annotation, ExecutableComponentDeclaration declaration) {

    Map<String, String> bindingMap = new HashMap<>();
    for (Binding binding : annotation.bindings()) {
      bindingMap.put(binding.actingParameter(), binding.extractionExpression());
    }

    List<ParameterDeclaration> allParameters = declaration.getAllParameters();
    Map<String, String> parameterNames = getContainerParameterNames(allParameters);

    Class<? extends SampleDataProvider> resolverClass = annotation.value();
    SampleDataProviderFactoryModelPropertyBuilder propertyBuilder =
        new SampleDataProviderFactoryModelPropertyBuilder(resolverClass);

    ParameterizableTypeWrapper resolverClassWrapper =
        new ParameterizableTypeWrapper(resolverClass, new DefaultExtensionsTypeLoaderFactory().createTypeLoader());

    List<ExtensionParameter> resolverParameters = resolverClassWrapper.getParametersAnnotatedWith(Parameter.class);
    resolverParameters.addAll(resolverClassWrapper.getParametersAnnotatedWith(com.zernova.sdk.api.annotation.param.Parameter.class));

    resolverParameters.forEach(param -> propertyBuilder
        .withInjectableParameter(param.getName(), param.getType().asMetadataType(), param.isRequired(),
                                 bindingMap.getOrDefault(param.getName(),
                                                         parameterNames.getOrDefault(param.getName(), param.getName()))));

    Reference<Boolean> requiresConfiguration = new Reference<>(false);
    Reference<Boolean> requiresConnection = new Reference<>(false);

    enrichWithConnection(propertyBuilder, resolverClassWrapper).ifPresent(field -> requiresConnection.set(true));
    enrichWithConfiguration(propertyBuilder, resolverClassWrapper).ifPresent(field -> requiresConfiguration.set(true));

    declaration.addModelProperty(propertyBuilder.build());

    return new SampleDataProviderModel(getActingParametersModel(resolverParameters, parameterNames, bindingMap),
                                       getSampleDataProviderId(resolverClass),
                                       requiresConfiguration.get(),
                                       requiresConnection.get());
  }

  /**
   * Introspects the given {@link ParameterizableTypeWrapper parameterizableComponent} looking if this ones uses either a
   * {@link Connection} or {@link com.zernova.sdk.api.annotation.param.Connection}. If any is true this method will indicate to the
   * {@link SampleDataProviderFactoryModelPropertyBuilder} that the correspondent {@link SampleDataProvider} will require a
   * connection.
   *
   * @param modelPropertyBuilder     the model property builder
   * @param parameterizableComponent component to introspect
   */
  private Optional<Field> enrichWithConnection(SampleDataProviderFactoryModelPropertyBuilder modelPropertyBuilder,
                                               ParameterizableTypeWrapper parameterizableComponent) {
    List<FieldElement> connectionFields = parameterizableComponent.getAnnotatedFields(Connection.class,
                                                                                      com.zernova.sdk.api.annotation.param.Connection.class);
    if (!connectionFields.isEmpty()) {
      Field field = connectionFields.get(0).getField().get();
      modelPropertyBuilder.withConnection(field);
      return of(field);
    }
    return empty();
  }

  /**
   * Introspects the given {@link ParameterizableTypeWrapper parameterizableComponent} looking if this ones uses either a
   * {@link Config} or {@link com.zernova.sdk.api.annotation.param.Config}, if any is true this method will indicate to the
   * {@link SampleDataProviderFactoryModelPropertyBuilder} that the correspondent {@link SampleDataProvider} will require a
   * config.
   *
   * @param modelPropertyBuilder     the property builder
   * @param parameterizableComponent component to introspect
   */
  private Optional<Field> enrichWithConfiguration(SampleDataProviderFactoryModelPropertyBuilder modelPropertyBuilder,
                                                  ParameterizableTypeWrapper parameterizableComponent) {
    List<FieldElement> configFields = parameterizableComponent.getAnnotatedFields(Config.class,
                                                                                  com.zernova.sdk.api.annotation.param.Config.class);
    if (!configFields.isEmpty()) {
      Field field = configFields.get(0).getField().get();
      modelPropertyBuilder.withConfig(field);
      return of(field);
    }
    return empty();
  }

  private List<ActingParameterModel> getActingParametersModel(List<ExtensionParameter> parameterDeclarations,
                                                              Map<String, String> parameterNames,
                                                              Map<String, String> bindings) {
    return parameterDeclarations.stream()
        .map(extensionParameter -> bindings.containsKey(extensionParameter.getName())
            ? new ImmutableActingParameterModel(extensionParameter.getName(), extensionParameter.isRequired(),
                                                bindings.get(extensionParameter.getName()))
            : new ImmutableActingParameterModel(parameterNames.getOrDefault(extensionParameter.getName(),
                                                                            extensionParameter.getName()),
                                                extensionParameter.isRequired(),
                                                parameterNames.getOrDefault(extensionParameter.getName(),
                                                                            extensionParameter.getName())))
        .collect(Collectors.toList());
  }

  private Map<String, String> getContainerParameterNames(List<ParameterDeclaration> allParameters) {
    Map<String, String> parameterNames = new HashMap<>();
    for (ParameterDeclaration parameterDeclaration : allParameters) {
      parameterNames.put(getImplementingName(parameterDeclaration), parameterDeclaration.getName());
    }
    return parameterNames;
  }
}
