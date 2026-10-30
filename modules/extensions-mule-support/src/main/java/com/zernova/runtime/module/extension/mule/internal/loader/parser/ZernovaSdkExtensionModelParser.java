/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader.parser;

import static com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType.OPERATION_DEF;
import static com.zernova.runtime.extension.api.dsl.syntax.DslSyntaxUtils.getSanitizedElementName;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getValidatedJavaVersionsIntersection;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.zernova.metadata.api.TypeLoader;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.model.ExternalLibraryModel;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.notification.NotificationModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.model.ExtensionModelHelper;
import com.zernova.runtime.extension.api.loader.parser.ConfigurationModelParser;
import com.zernova.runtime.extension.api.loader.parser.ConnectionProviderModelParser;
import com.zernova.runtime.extension.api.loader.parser.ErrorModelParser;
import com.zernova.runtime.extension.api.loader.parser.ExtensionModelParser;
import com.zernova.runtime.extension.api.loader.parser.FunctionModelParser;
import com.zernova.runtime.extension.api.loader.parser.OperationModelParser;
import com.zernova.runtime.extension.api.loader.parser.SourceModelParser;
import com.zernova.runtime.extension.api.runtime.exception.SdkExceptionHandlerFactory;
import com.zernova.sdk.api.artifact.lifecycle.ArtifactLifecycleListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * {@link ExtensionModelParser} implementation for Mule SDK extensions
 *
 * @since 4.5.0
 */
public abstract class ZernovaSdkExtensionModelParser extends BaseZernovaSdkExtensionModelParser implements ExtensionModelParser {

  private final TypeLoader typeLoader;
  private List<OperationModelParser> operationModelParsers;
  private final ExtensionModelHelper extensionModelHelper;
  private Set<String> supportedJavaVersions;

  public ZernovaSdkExtensionModelParser(TypeLoader typeLoader, ExtensionModelHelper extensionModelHelper) {
    this.typeLoader = typeLoader;
    this.extensionModelHelper = extensionModelHelper;
  }

  protected void init(ArtifactAst ast) {
    supportedJavaVersions = getValidatedJavaVersionsIntersection(getName(), "Extension", ast.dependencies());
    operationModelParsers = computeOperationModelParsers(ast);
  }

  @Override
  public List<ModelProperty> getAdditionalModelProperties() {
    return emptyList();
  }

  @Override
  public List<ConfigurationModelParser> getConfigurationParsers() {
    return emptyList();
  }

  @Override
  public List<OperationModelParser> getOperationModelParsers() {
    return operationModelParsers;
  }

  @Override
  public List<SourceModelParser> getSourceModelParsers() {
    return emptyList();
  }

  @Override
  public List<ConnectionProviderModelParser> getConnectionProviderModelParsers() {
    return emptyList();
  }

  @Override
  public List<FunctionModelParser> getFunctionModelParsers() {
    return emptyList();
  }

  @Override
  public List<ErrorModelParser> getErrorModelParsers() {
    return emptyList();
  }

  @Override
  public List<ExternalLibraryModel> getExternalLibraryModels() {
    return emptyList();
  }

  @Override
  public Optional<SdkExceptionHandlerFactory> getExceptionHandlerFactory() {
    return empty();
  }

  @Override
  public Optional<DeprecationModel> getDeprecationModel() {
    return empty();
  }

  @Override
  public List<MetadataType> getExportedTypes() {
    return emptyList();
  }

  @Override
  public List<String> getExportedResources() {
    return emptyList();
  }

  @Override
  public List<MetadataType> getImportedTypes() {
    return emptyList();
  }

  @Override
  public List<String> getPrivilegedExportedArtifacts() {
    return emptyList();
  }

  @Override
  public List<String> getPrivilegedExportedPackages() {
    return emptyList();
  }

  @Override
  public Map<MetadataType, List<MetadataType>> getSubTypes() {
    return emptyMap();
  }

  @Override
  public List<NotificationModel> getNotificationModels() {
    return emptyList();
  }

  @Override
  public Optional<Class<? extends ArtifactLifecycleListener>> getArtifactLifecycleListenerClass() {
    return empty();
  }

  @Override
  public Set<String> getSupportedJavaVersions() {
    return supportedJavaVersions;
  }

  /**
   * @param ast the {@link ArtifactAst} representing the extension.
   * @return a {@link Stream} with the top level elements {@link ComponentAst} extracted from the {@code ast}.
   */
  protected abstract Stream<ComponentAst> getTopLevelElements(ArtifactAst ast);

  private List<OperationModelParser> computeOperationModelParsers(ArtifactAst ast) {
    final Map<String, ZernovaSdkOperationModelParser> operationParsersByName =
        getTopLevelElements(ast)
            .filter(c -> c.getComponentType() == OPERATION_DEF)
            .map(c -> createOperationModelParser(c, getNamespace()))
            .collect(toMap(c -> getSanitizedElementName(c::getName), identity()));

    // Some characteristics of the operation model parsers require knowledge about the other operation model parsers
    operationParsersByName.values()
        .forEach(operationModelParser -> operationModelParser.computeCharacteristics(operationParsersByName));

    return new ArrayList<>(operationParsersByName.values());
  }

  protected ZernovaSdkOperationModelParser createOperationModelParser(ComponentAst operation, String namespace) {
    return new ZernovaSdkOperationModelParser(operation, namespace, typeLoader, extensionModelHelper);
  }
}
