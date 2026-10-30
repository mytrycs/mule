/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation.test;

import static com.zernova.runtime.api.dsl.DslResolvingContext.nullDslResolvingContext;
import static com.zernova.runtime.api.meta.Category.COMMUNITY;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.recursiveStreamWithHierarchy;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.MULESOFT_VENDOR;
import static com.zernova.runtime.core.extension.ComponentConfigurerTestUtils.createMockedFactory;
import static com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest.builder;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConfigurationDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclarer;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;
import com.zernova.runtime.ast.api.xml.AstXmlParser;
import com.zernova.runtime.config.internal.dsl.utils.DslConstants;
import com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider;
import com.zernova.runtime.core.internal.extension.CustomBuildingDefinitionProviderModelProperty;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import org.apache.commons.io.input.ReaderInputStream;

import org.junit.Before;

public abstract class AbstractCoreValidationTestCase {

  protected static AstXmlParser parser;

  @Before
  public void createAstXmlParser() {
    // Avoid recreating the same parser
    if (parser != null) {
      return;
    }

    final ClassTypeLoader typeLoader = ExtensionsTypeLoaderFactory.getDefault()
        .createTypeLoader(AbstractCoreValidationTestCase.class.getClassLoader());

    ExtensionDeclarer extensionDeclarer = new ExtensionDeclarer()
        .named("test")
        .describedAs("test")
        .onVersion("1.0.0")
        .fromVendor(MULESOFT_VENDOR)
        .withCategory(COMMUNITY)
        .withModelProperty(new CustomBuildingDefinitionProviderModelProperty())
        .withXmlDsl(XmlDslModel.builder()
            .setPrefix("test")
            .setNamespace(format(DslConstants.DEFAULT_NAMESPACE_URI_MASK, "test"))
            .setSchemaVersion("1.0.0")
            .setXsdFileName("test.xsd")
            .setSchemaLocation(format("%s/%s/%s.xsd", format(DslConstants.DEFAULT_NAMESPACE_URI_MASK, "test"), "current", "test"))
            .build());

    final ConfigurationDeclarer config = extensionDeclarer.withConfig("config");
    final ConfigurationDeclarer otherConfig = extensionDeclarer.withConfig("otherConfig");
    otherConfig.onDefaultParameterGroup().withRequiredParameter("count").ofType(ExtensionsTypeLoaderFactory.getDefault()
        .createTypeLoader(ZernovaExtensionModelProvider.class.getClassLoader()).load(Integer.class));

    final OperationDeclarer operation = extensionDeclarer.withOperation("operation");
    operation.withOutput().ofType(typeLoader.load(void.class));
    operation.withOutputAttributes().ofType(typeLoader.load(void.class));

    parser = AstXmlParser.builder()
        .withExtensionModels(resolveRuntimeExtensionModels())
        .withExtensionModel(new ExtensionModelLoader() {

          @Override
          public String getId() {
            return AbstractCoreValidationTestCase.class.getName();
          }

          @Override
          protected void declareExtension(ExtensionLoadingContext context) {
            // nothing to do
          }
        }.loadExtensionModel(extensionDeclarer, builder(AbstractCoreValidationTestCase.class.getClassLoader(),
                                                        nullDslResolvingContext()).build()))
        .withSchemaValidationsDisabled()
        .build();
  }

  protected List<ExtensionModel> resolveRuntimeExtensionModels() {
    ZernovaExtensionModelProvider.setConfigurerFactory(createMockedFactory());
    return asList(ZernovaExtensionModelProvider.getExtensionModel());
  }

  protected List<ValidationResultItem> runValidation(String configFileNamePrefix, final String... xmlConfigs) {
    final List<Pair<String, InputStream>> configs = new ArrayList<>();

    for (int i = 0; i < xmlConfigs.length; i++) {
      configs.add(new Pair<>(configFileNamePrefix + i, new ReaderInputStream(new StringReader(xmlConfigs[i]), UTF_8)));
    }

    final ArtifactAst ast = parser.parse(configs);

    return recursiveStreamWithHierarchy(ast)
        .filter(c -> getValidation().applicable()
            .test(ImmutableList.<ComponentAst>builder().addAll(c.getSecond()).add(c.getFirst()).build()))
        .flatMap(c -> getValidation().validateMany(c.getFirst(), ast).stream())
        .collect(toList());
  }

  protected abstract Validation getValidation();
}
