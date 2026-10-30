/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader.parser;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;
import static com.zernova.runtime.core.api.util.StringUtils.isBlank;

import com.zernova.metadata.api.TypeLoader;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.display.LayoutModel;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.model.ExtensionModelHelper;
import com.zernova.runtime.extension.api.loader.parser.ParameterGroupModelParser;
import com.zernova.runtime.extension.api.loader.parser.ParameterModelParser;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * {@link ParameterGroupModelParser} implementation for Mule SDK
 *
 * @since 4.5.0
 */
class ZernovaSdkParameterGroupModelParser extends BaseZernovaSdkExtensionModelParser implements ParameterGroupModelParser {

  private final List<ParameterModelParser> parameters;
  private final Optional<ExclusiveOptionalDescriptor> exclusiveOptionals;
  private final ExtensionModelHelper extensionModelHelper;

  public ZernovaSdkParameterGroupModelParser(ComponentAst parametersComponent, TypeLoader typeLoader,
                                          ExtensionModelHelper extensionModelHelper) {
    this.extensionModelHelper = extensionModelHelper;
    parameters = doParserParameters(parametersComponent, typeLoader);
    exclusiveOptionals = doParseExclusiveOptionalDescriptorFromGroup(parametersComponent);
  }

  private List<ParameterModelParser> doParserParameters(ComponentAst parametersComponent, TypeLoader typeLoader) {
    Stream<ParameterModelParser> parameterParsers = getChildren(parametersComponent, "parameter")
        .map(p -> new ZernovaSdkParameterModelParser(p, typeLoader, extensionModelHelper));
    Stream<ParameterModelParser> optionalParameterParsers = getChildren(parametersComponent, "optional-parameter")
        .map(p -> new ZernovaSdkOptionalParameterModelParser(p, typeLoader, extensionModelHelper));

    return concat(parameterParsers, optionalParameterParsers).collect(toList());
  }

  private Optional<ExclusiveOptionalDescriptor> doParseExclusiveOptionalDescriptorFromGroup(ComponentAst parametersComponent) {
    return getSingleChild(parametersComponent, "exclusive-optionals")
        .map(exclusiveOptionals -> doParseExclusiveOptionalDescriptor(exclusiveOptionals));
  }

  private ExclusiveOptionalDescriptor doParseExclusiveOptionalDescriptor(ComponentAst exclusiveOptionals) {
    Set<String> parameters = Stream.of(this.<String>getParameter(exclusiveOptionals, "exclusiveOptionals").split(","))
        .map(String::trim)
        .filter(p -> !isBlank(p))
        .collect(toCollection(LinkedHashSet::new));

    return new ExclusiveOptionalDescriptor(parameters,
                                           (boolean) getOptionalParameter(exclusiveOptionals, "oneRequired").orElse(false));
  }

  @Override
  public String getName() {
    return DEFAULT_GROUP_NAME;
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public List<ParameterModelParser> getParameterParsers() {
    return parameters;
  }

  @Override
  public Optional<DisplayModel> getDisplayModel() {
    return empty();
  }

  @Override
  public Optional<LayoutModel> getLayoutModel() {
    return empty();
  }

  @Override
  public Optional<ExclusiveOptionalDescriptor> getExclusiveOptionals() {
    return exclusiveOptionals;
  }

  @Override
  public boolean showsInDsl() {
    return false;
  }

  @Override
  public List<ModelProperty> getAdditionalModelProperties() {
    return emptyList();
  }
}
