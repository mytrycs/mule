/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java;

import static com.zernova.runtime.extension.api.annotation.Extension.DEFAULT_CONFIG_NAME;
import static com.zernova.runtime.module.extension.api.util.ZernovaExtensionUtils.isAddAnnotationsToConfigClass;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.mapReduceSingleAnnotation;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ParameterDeclarationContext.forConfig;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.lib.JavaExternalLibModelParserUtils.parseExternalLibraryModels;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.stereotypes.JavaStereotypeModelParserUtils.resolveStereotype;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.utils.MinZernovaVersionUtils.getContainerAnnotationMinMuleVersion;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.utils.MinZernovaVersionUtils.resolveConfigurationMinMuleVersion;

import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.zernova.runtime.api.meta.model.ExternalLibraryModel;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.NoImplicit;
import com.zernova.runtime.extension.api.exception.IllegalConfigurationModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.parser.MinZernovaVersionParser;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationFactory;
import com.zernova.runtime.module.extension.api.loader.java.type.ComponentElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionElement;
import com.zernova.runtime.module.extension.api.loader.java.type.OperationContainerElement;
import com.zernova.runtime.module.extension.internal.loader.java.TypeAwareConfigurationFactory;
import com.zernova.runtime.module.extension.internal.loader.java.property.ImplementingTypeModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionTypeDescriptorModelProperty;
import com.zernova.runtime.extension.api.loader.parser.ConfigurationModelParser;
import com.zernova.runtime.extension.api.loader.parser.ConnectionProviderModelParser;
import com.zernova.runtime.extension.api.loader.parser.FunctionModelParser;
import com.zernova.runtime.extension.api.loader.parser.OperationModelParser;
import com.zernova.runtime.extension.api.loader.parser.ParameterGroupModelParser;
import com.zernova.runtime.extension.api.loader.parser.SourceModelParser;
import com.zernova.runtime.extension.api.loader.parser.StereotypeModelFactory;
import com.zernova.sdk.api.annotation.Configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@link ConfigurationModelParser} for Java based syntax
 *
 * @since 4.5.0
 */
public class JavaConfigurationModelParser extends AbstractJavaModelParser implements ConfigurationModelParser {

  private final JavaExtensionModelParser extensionModelParser;
  private final ComponentElement configElement;

  public JavaConfigurationModelParser(JavaExtensionModelParser extensionModelParser,
                                      ExtensionElement extensionElement,
                                      ComponentElement configElement,
                                      ExtensionLoadingContext loadingContext) {
    super(extensionElement, loadingContext);
    this.extensionModelParser = extensionModelParser;
    this.configElement = configElement;

    parseStructure();
  }

  private void parseStructure() {
    checkConfigurationIsNotAnOperation(extensionElement, configElement);
    additionalModelProperties.add(new ImplementingTypeModelProperty(configElement.getDeclaringClass().orElse(Object.class)));
    additionalModelProperties.add(new ExtensionTypeDescriptorModelProperty(configElement));
  }

  @Override
  public String getName() {
    return mapReduceSingleAnnotation(configElement,
                                     "Configuration", configElement.getName(),
                                     Configuration.class,
                                     com.zernova.sdk.api.annotation.Configuration.class,
                                     value -> value.getStringValue(Configuration::name),
                                     value -> value
                                         .getStringValue(com.zernova.sdk.api.annotation.Configuration::name))
                                             .map(name -> isBlank(name) ? DEFAULT_CONFIG_NAME : name)
                                             .orElse(DEFAULT_CONFIG_NAME);
  }

  @Override
  public String getDescription() {
    return configElement.getDescription();
  }

  @Override
  public List<ParameterGroupModelParser> getParameterGroupParsers() {
    return JavaExtensionModelParserUtils.getParameterGroupParsers(configElement.getParameters(),
                                                                  forConfig(configElement.getName(),
                                                                            loadingContext));
  }

  @Override
  public List<OperationModelParser> getOperationParsers() {
    List<OperationModelParser> operationModelParsers = new ArrayList<>(JavaExtensionModelParserUtils.getOperationParsers(
                                                                                                                         extensionModelParser,
                                                                                                                         extensionElement,
                                                                                                                         configElement,
                                                                                                                         loadingContext)
        .collect(toList()));
    sort(operationModelParsers, comparing(OperationModelParser::getName));
    return unmodifiableList(operationModelParsers);
  }

  @Override
  public List<SourceModelParser> getSourceModelParsers() {
    return JavaExtensionModelParserUtils.getSourceParsers(extensionElement, configElement.getSources(), loadingContext)
        .collect(toList());
  }

  @Override
  public List<ConnectionProviderModelParser> getConnectionProviderModelParsers() {
    return JavaExtensionModelParserUtils.getConnectionProviderModelParsers(extensionElement,
                                                                           configElement.getConnectionProviders(),
                                                                           loadingContext);
  }

  @Override
  public List<FunctionModelParser> getFunctionModelParsers() {
    return JavaExtensionModelParserUtils.getFunctionModelParsers(extensionElement,
                                                                 configElement.getFunctionContainers(),
                                                                 loadingContext);
  }

  @Override
  public ConfigurationFactory getConfigurationFactory() {
    Class<?> extensionClass = extensionElement.getDeclaringClass().orElse(Object.class);
    Class<?> configClass = configElement.getDeclaringClass().orElse(Object.class);

    ClassLoader classLoader = extensionClass.getClassLoader() != null
        ? extensionClass.getClassLoader()
        : Thread.currentThread().getContextClassLoader();

    return new TypeAwareConfigurationFactory(configClass, classLoader, isAddAnnotationsToConfigClass(loadingContext));
  }

  @Override
  public boolean isForceNoImplicit() {
    return configElement.isAnnotatedWith(NoImplicit.class) ||
        configElement.isAnnotatedWith(com.zernova.sdk.api.annotation.NoImplicit.class);
  }

  @Override
  public List<ExternalLibraryModel> getExternalLibraryModels() {
    return parseExternalLibraryModels(configElement);
  }

  @Override
  public Optional<StereotypeModel> getStereotype(StereotypeModelFactory factory) {
    return resolveStereotype(configElement, "Configuration", getName(), factory);
  }

  private void checkConfigurationIsNotAnOperation(ExtensionElement extensionElement, ComponentElement componentElement) {
    List<OperationContainerElement> allOperations = new ArrayList<>();
    allOperations.addAll(extensionElement.getOperationContainers());
    allOperations.addAll(componentElement.getOperationContainers());

    for (OperationContainerElement operationClass : allOperations) {
      if (componentElement.isAssignableFrom(operationClass)
          || componentElement.isAssignableTo(operationClass)) {
        throw new IllegalConfigurationModelDefinitionException(
                                                               format("Configuration class '%s' cannot be the same class (nor a derivative) of any operation class '%s",
                                                                      componentElement.getName(), operationClass.getName()));
      }
    }
  }

  @Override
  public Optional<DeprecationModel> getDeprecationModel() {
    return JavaExtensionModelParserUtils.getDeprecationModel(configElement);
  }

  @Override
  public Optional<DisplayModel> getDisplayModel() {
    return JavaExtensionModelParserUtils.getDisplayModel(configElement, "configuration", configElement.getName());
  }

  @Override
  public Optional<MinZernovaVersionParser> getResolvedMinMuleVersion() {
    return of(resolveConfigurationMinMuleVersion(configElement,
                                                 getContainerAnnotationMinMuleVersion(extensionElement,
                                                                                      Configurations.class,
                                                                                      Configurations::value,
                                                                                      configElement)));
  }
}
