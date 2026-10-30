/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.utils;

import static com.zernova.runtime.core.api.util.StringUtils.isBlank;
import static com.zernova.runtime.extension.api.util.XmlModelUtils.createXmlLanguageModel;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterizedDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.WithSemanticTermsDeclaration;
import com.zernova.runtime.api.metadata.resolving.NamedTypeResolver;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.extension.api.metadata.ComponentMetadataConfigurer;
import com.zernova.runtime.extension.api.metadata.ComponentMetadataConfigurerFactory;
import com.zernova.runtime.extension.api.metadata.NullMetadataResolver;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionElement;
import com.zernova.runtime.extension.api.loader.delegate.ModelLoaderDelegate;
import com.zernova.runtime.extension.api.loader.parser.AttributesResolverModelParser;
import com.zernova.runtime.extension.api.loader.parser.SemanticTermsParser;
import com.zernova.runtime.extension.api.loader.parser.XmlDslConfiguration;
import com.zernova.runtime.extension.api.loader.parser.metadata.InputResolverModelParser;
import com.zernova.runtime.extension.api.loader.parser.metadata.MetadataKeyModelParser;
import com.zernova.runtime.extension.api.loader.parser.metadata.OutputResolverModelParser;
import com.zernova.runtime.extension.api.loader.parser.metadata.RoutesChainInputTypesResolverModelParser;
import com.zernova.runtime.extension.api.loader.parser.metadata.ScopeChainInputTypeResolverModelParser;
import com.zernova.sdk.api.annotation.dsl.xml.Xml;

import java.util.List;
import java.util.Optional;

/**
 * Utility class for {@link ModelLoaderDelegate model loaders}
 *
 * @since 4.0
 */
public final class ModelLoaderUtils {

  private ModelLoaderUtils() {}

  /**
   * Adds all the semantic terms in the {@code parser} into the given {@code declaration}
   *
   * @param declaration a declaration
   * @param parser      a parser
   * @since 4.5.0
   */
  public static void addSemanticTerms(WithSemanticTermsDeclaration declaration, SemanticTermsParser parser) {
    declaration.getSemanticTerms().addAll(parser.getSemanticTerms());
  }

  /**
   * Utility method to obtain a default {@link XmlDslModel} of a given {@link XmlDslConfiguration}
   *
   * @param extensionName                 the name of the extension
   * @param version                       version of the extension
   * @param xmlDslAnnotationConfiguration configuration of {@link com.zernova.runtime.extension.api.annotation.dsl.xml.Xml} of
   *                                      {@link Xml}
   * @return the {@link XmlDslModel}
   * @since 4.5.0
   */
  public static XmlDslModel getXmlDslModel(String extensionName,
                                           String version,
                                           Optional<XmlDslConfiguration> xmlDslAnnotationConfiguration) {
    Optional<String> prefix = empty();
    Optional<String> namespace = empty();

    if (xmlDslAnnotationConfiguration.isPresent()) {
      prefix = of(xmlDslAnnotationConfiguration.get().getPrefix());
      namespace = of(xmlDslAnnotationConfiguration.get().getNamespace());
    }

    return createXmlLanguageModel(prefix, namespace, extensionName, version);
  }

  /**
   * Utility method to obtain a default {@link XmlDslModel} of a given {@link XmlDslConfiguration}
   *
   * @param extensionElement              the extension element
   * @param version                       version of the extension
   * @param xmlDslAnnotationConfiguration configuration of {@link com.zernova.runtime.extension.api.annotation.dsl.xml.Xml} of
   *                                      {@link Xml}
   * @return the {@link XmlDslModel}
   * @since 4.5.0
   */
  public static XmlDslModel getXmlDslModel(ExtensionElement extensionElement,
                                           String version,
                                           Optional<XmlDslConfiguration> xmlDslAnnotationConfiguration) {
    return getXmlDslModel(extensionElement.getName(), version, xmlDslAnnotationConfiguration);
  }

  /**
   * Declares all the model properties necessary to enable the parsed resolved
   *
   * @param declaration                   the declaration
   * @param outputResolverModelParser     parser with the output metadata
   * @param attributesResolverModelParser parser with the attributes metadata
   * @param inputResolverModelParsers     parser with the input metadata
   * @param keyIdResolverModelParser      parser with the key id metadata
   *
   * @since 4.7.0
   */
  public static void declareMetadataModelProperties(ParameterizedDeclaration declaration,
                                                    Optional<OutputResolverModelParser> outputResolverModelParser,
                                                    Optional<AttributesResolverModelParser> attributesResolverModelParser,
                                                    List<InputResolverModelParser> inputResolverModelParsers,
                                                    Optional<MetadataKeyModelParser> keyIdResolverModelParser,
                                                    boolean connected) {
    declareMetadataModelProperties(declaration,
                                   outputResolverModelParser,
                                   attributesResolverModelParser,
                                   inputResolverModelParsers,
                                   keyIdResolverModelParser,
                                   connected,
                                   empty(),
                                   empty());
  }

  /**
   * Declares all the model properties necessary to enable the parsed resolved
   *
   * @param declaration                         the declaration
   * @param outputResolverModelParser           parser with the output metadata
   * @param attributesResolverModelParser       parser with the attributes metadata
   * @param inputResolverModelParsers           parser with the input metadata
   * @param keyIdResolverModelParser            parser with the key id metadata
   * @param scopeChainInputResolverParser       parser with chain input resolver
   * @param routesChainInputTypesResolverParser parser with routes chain input resolvers
   * @since 4.7.0
   */
  public static void declareMetadataModelProperties(ParameterizedDeclaration declaration,
                                                    Optional<OutputResolverModelParser> outputResolverModelParser,
                                                    Optional<AttributesResolverModelParser> attributesResolverModelParser,
                                                    List<InputResolverModelParser> inputResolverModelParsers,
                                                    Optional<MetadataKeyModelParser> keyIdResolverModelParser,
                                                    boolean connected,
                                                    Optional<ScopeChainInputTypeResolverModelParser> scopeChainInputResolverParser,
                                                    Optional<RoutesChainInputTypesResolverModelParser> routesChainInputTypesResolverParser) {
    if (outputResolverModelParser.map(p -> p.hasOutputResolver()).orElse(false)
        || !inputResolverModelParsers.isEmpty()
        || keyIdResolverModelParser.map(p -> p.hasKeyIdResolver()).orElse(false)
        || scopeChainInputResolverParser.isPresent()
        || routesChainInputTypesResolverParser.isPresent()) {
      final ComponentMetadataConfigurer configurer =
          ComponentMetadataConfigurerFactory.getDefault().create().setConnected(connected);
      outputResolverModelParser.ifPresent(p -> configurer.setOutputTypeResolver(p.getOutputResolver()));
      attributesResolverModelParser.ifPresent(p -> configurer.setAttributesTypeResolver(p.getAttributesResolver()));
      keyIdResolverModelParser.ifPresent(p -> configurer.setKeysResolver(p.getKeyResolver(), p.getParameterName(),
                                                                         p.getMetadataType(), p.isPartialKeyResolver()));

      inputResolverModelParsers
          .forEach(parser -> configurer.addInputResolver(parser.getParameterName(), parser.getInputResolver()));
      scopeChainInputResolverParser.ifPresent(p -> configurer.setChainInputTypeResolver(p.getChainInputTypeResolver()));
      routesChainInputTypesResolverParser
          .ifPresent(p -> configurer.addRoutesChainInputResolvers(p.getRoutesChainInputResolvers()));

      configurer.configure(declaration);
    } else {
      ComponentMetadataConfigurerFactory.getDefault().create().configureNullMetadata(declaration);
    }
  }

  /**
   * Given the parser of the resolvers of output, input and key id metadata return the category name of the resolvers.
   *
   * @param outputResolverModelParser parser with the output metadata
   * @param inputResolverModelParsers parser with the input metadata
   * @param metadataKeyModelParser    parser with the key id metadata
   *
   * @return the category name of the resolvers if present, null otherwise
   *
   * @since 4.5.0
   */
  public static String getCategoryName(MetadataKeyModelParser metadataKeyModelParser,
                                       List<InputResolverModelParser> inputResolverModelParsers,
                                       OutputResolverModelParser outputResolverModelParser) {
    if (metadataKeyModelParser != null && metadataKeyModelParser.hasKeyIdResolver()) {
      return metadataKeyModelParser.getKeyResolver().getCategoryName();
    } else if (!inputResolverModelParsers.isEmpty()) {
      return inputResolverModelParsers.iterator().next().getInputResolver().getCategoryName();
    } else if (outputResolverModelParser != null && outputResolverModelParser.hasOutputResolver()) {
      return outputResolverModelParser.getOutputResolver().getCategoryName();
    } else {
      return null;
    }
  }

  public static String getCategoryName(TypeKeysResolver typeKeysResolver,
                                       String firstSeenInputResolverCategory,
                                       OutputTypeResolver outputTypeResolver) {
    if (isNotNull(typeKeysResolver)) {
      return typeKeysResolver.getCategoryName();
    } else if (!isBlank(firstSeenInputResolverCategory)) {
      return firstSeenInputResolverCategory;
    } else if (isNotNull(outputTypeResolver)) {
      return outputTypeResolver.getCategoryName();
    } else {
      return null;
    }
  }

  private static boolean isNotNull(Object resolver) {
    if (resolver == null) {
      return false;
    }
    if (resolver instanceof NamedTypeResolver) {
      return !(resolver instanceof NullMetadataResolver);
    } else if (resolver instanceof com.zernova.sdk.api.metadata.resolving.NamedTypeResolver) {
      return !(resolver instanceof com.zernova.sdk.api.metadata.NullMetadataResolver);
    }

    return true;
  }
}
