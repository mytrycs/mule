/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java;

import static com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserUtils.getConfigParameter;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserUtils.getParameterGroupParsers;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ParameterDeclarationContext.forFunction;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.semantics.SemanticTermsParserUtils.addCustomTerms;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.utils.MinZernovaVersionUtils.getContainerAnnotationMinZernovaVersion;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.utils.MinZernovaVersionUtils.resolveFunctionMinZernovaVersion;

import static java.lang.String.format;
import static java.util.Objects.hash;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.exception.IllegalOperationModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.runtime.function.FunctionExecutorFactory;
import com.zernova.runtime.extension.api.loader.parser.MinZernovaVersionParser;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.api.loader.java.type.FunctionContainerElement;
import com.zernova.runtime.module.extension.api.loader.java.type.FunctionElement;
import com.zernova.runtime.module.extension.internal.loader.java.property.ImplementingMethodModelProperty;
import com.zernova.runtime.module.extension.internal.loader.parser.DefaultOutputModelParser;
import com.zernova.runtime.extension.api.loader.parser.FunctionModelParser;
import com.zernova.runtime.extension.api.loader.parser.ParameterGroupModelParser;
import com.zernova.runtime.module.extension.internal.runtime.function.ReflectiveFunctionExecutorFactory;
import com.zernova.runtime.module.extension.internal.util.IntrospectionUtils;
import com.zernova.sdk.api.annotation.ExpressionFunctions;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@link FunctionModelParser} for Java based syntax
 *
 * @since 4.5.0
 */
public class JavaFunctionModelParser extends AbstractJavaExecutableComponentModelParser implements FunctionModelParser {

  private final FunctionElement functionElement;

  public JavaFunctionModelParser(ExtensionElement extensionElement,
                                 FunctionElement functionElement,
                                 ExtensionLoadingContext loadingContext) {
    super(extensionElement, loadingContext);

    this.functionElement = functionElement;

    if (!isIgnored()) {
      parseStructure();
      collectAdditionalModelProperties();
    }
  }

  @Override
  public String getName() {
    return functionElement.getAlias();
  }

  @Override
  public String getDescription() {
    return functionElement.getDescription();
  }

  @Override
  public List<ParameterGroupModelParser> getParameterGroupModelParsers() {
    return getParameterGroupParsers(functionElement.getParameters(), forFunction(getName(), loadingContext));
  }

  @Override
  public boolean isIgnored() {
    return IntrospectionUtils.isIgnored(functionElement, loadingContext);
  }

  @Override
  public Optional<FunctionExecutorFactory> getFunctionExecutorFactory() {
    if (functionElement.getMethod().isPresent()) {
      return of(new ReflectiveFunctionExecutorFactory<>(functionElement.getDeclaringClass().get(),
                                                        functionElement.getMethod().get()));
    } else {
      return empty();
    }
  }

  @Override
  public Set<String> getSemanticTerms() {
    Set<String> terms = new LinkedHashSet<>();
    addCustomTerms(functionElement, terms);

    return terms;
  }

  @Override
  protected String getComponentTypeName() {
    return "Function";
  }

  private void collectAdditionalModelProperties() {
    functionElement.getMethod().map(ImplementingMethodModelProperty::new)
        .ifPresent(additionalModelProperties::add);
  }

  private void parseStructure() {
    checkIsNotAnExtension();

    final Optional<ExtensionParameter> configParameter = getConfigParameter(functionElement);
    if (configParameter.isPresent()) {
      throw new IllegalModelDefinitionException(format("Function '%s' requires a config parameter, but that is not allowed. "
          + "Remove such parameter.", getName()));
    }

    outputType = new DefaultOutputModelParser(functionElement.getReturnMetadataType(), false);
  }

  private void checkIsNotAnExtension() {
    final FunctionContainerElement type = functionElement.getEnclosingType();
    if (type.isAssignableFrom(extensionElement) || extensionElement.isAssignableFrom(type)) {
      throw new IllegalOperationModelDefinitionException(
                                                         format("Function class '%s' cannot be the same class (nor a derivative) of the extension class '%s",
                                                                type.getName(), extensionElement.getName()));
    }
  }

  @Override
  public Optional<DeprecationModel> getDeprecationModel() {
    return JavaExtensionModelParserUtils.getDeprecationModel(functionElement);
  }

  @Override
  public Optional<MinZernovaVersionParser> getResolvedMinZernovaVersion() {
    return of(resolveFunctionMinZernovaVersion(functionElement,
                                            getContainerAnnotationMinZernovaVersion(extensionElement,
                                                                                 ExpressionFunctions.class,
                                                                                 ExpressionFunctions::value,
                                                                                 functionElement
                                                                                     .getEnclosingType())));
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof JavaFunctionModelParser) {
      return functionElement.equals(((JavaFunctionModelParser) o).functionElement);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return hash(functionElement);
  }
}
