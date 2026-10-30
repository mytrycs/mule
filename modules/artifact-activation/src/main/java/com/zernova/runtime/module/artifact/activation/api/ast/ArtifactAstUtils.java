/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.api.ast;

import static com.zernova.runtime.api.functional.Either.left;
import static com.zernova.runtime.api.functional.Either.right;
import static com.zernova.runtime.module.artifact.activation.internal.ast.ArtifactAstUtils.loadConfigResources;
import static com.zernova.runtime.module.artifact.activation.internal.ast.ArtifactAstUtils.parseArtifact;

import static java.util.Optional.empty;

import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ArtifactType;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.dsl.api.ConfigResource;
import com.zernova.runtime.module.artifact.activation.internal.ast.ZernovaSdkApplicationExtensionModelLoadingMediator;
import com.zernova.runtime.module.artifact.activation.internal.ast.ZernovaSdkExtensionModelLoadingMediator;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.w3c.dom.Document;

/**
 * Utilities for parsing and handling {@link ArtifactAst}
 *
 * @since 4.5.0
 */
public final class ArtifactAstUtils {

  /**
   * Parses {@code configResources} for a Mule application and returns an {@link ArtifactAst} enriched with an additional
   * {@link ExtensionModel} which models the app itself, with all its defined operations, sources, functions, etc.
   * <p>
   * This extra {@link ExtensionModel} is accessible through the {@link ArtifactAst#dependencies()} set its named after the
   * {@code muleContext.getConfiguration.getId()} return value
   *
   * @param configResources                   the paths to the application's config files
   * @param parserSupplier                    the supplier used to obtain the ast parser. It might be invoked several times during
   *                                          the parsing
   * @param extensions                        the initial set of extensions the app depends on.
   * @param artifactType                      the artifact type
   * @param disableValidations                whether to disable DSL validation
   * @param muleContext                       the app's {@link ZernovaContext}
   * @param expressionLanguageMetadataService the {@link ExpressionLanguageMetadataService} used to resolve types.
   * @return an {@link ArtifactAst}
   * @throws ConfigurationException it the app couldn't be parsed
   * @deprecated Use
   *             {@link #parseAndBuildAppExtensionModel(String[], AstXmlParserSupplier, Set, boolean, ClassLoader, ZernovaConfiguration, ExpressionLanguageMetadataService)}
   *             instead;
   */
  @Deprecated
  public static ArtifactAst parseAndBuildAppExtensionModel(String[] configResources,
                                                           AstXmlParserSupplier parserSupplier,
                                                           Set<ExtensionModel> extensions,
                                                           ArtifactType artifactType,
                                                           boolean disableValidations,
                                                           ZernovaContext muleContext,
                                                           ExpressionLanguageMetadataService expressionLanguageMetadataService)
      throws ConfigurationException {
    return parseArtifact(null,
                         left(loadConfigResources(configResources, muleContext.getExecutionClassLoader())),
                         parserSupplier,
                         extensions,
                         disableValidations,
                         muleContext.getExecutionClassLoader(),
                         getExtensionModelLoadingMediator(muleContext.getConfiguration().getId(),
                                                          muleContext.getConfiguration().getArtifactCoordinates(),
                                                          expressionLanguageMetadataService));
  }

  /**
   * Parses {@code configResources} for a Mule application and returns an {@link ArtifactAst} enriched with an additional
   * {@link ExtensionModel} which models the app itself, with all its defined operations, sources, functions, etc.
   * <p>
   * This extra {@link ExtensionModel} is accessible through the {@link ArtifactAst#dependencies()} set its named after the
   * {@code muleContext.getConfiguration.getId()} return value
   *
   * @param artifactName                      the name of the artifact whose configs will be parsed
   * @param configResources                   the paths to the application's config files
   * @param parserSupplier                    the supplier used to obtain the ast parser. It might be invoked several times during
   *                                          the parsing
   * @param extensions                        the initial set of extensions the app depends on.
   * @param artifactType                      the artifact type
   * @param disableValidations                whether to disable DSL validation
   * @param muleContext                       the app's {@link ZernovaContext}
   * @param expressionLanguageMetadataService the {@link ExpressionLanguageMetadataService} used to resolve types.
   * @return an {@link ArtifactAst}
   * @throws ConfigurationException it the app couldn't be parsed
   *
   * @since 4.8
   * @deprecated Use
   *             {@link #parseAndBuildAppExtensionModel(String, String[], AstXmlParserSupplier, Set, boolean, ClassLoader, ZernovaConfiguration, ExpressionLanguageMetadataService)}
   *             instead.
   */
  @Deprecated
  public static ArtifactAst parseAndBuildAppExtensionModel(String artifactName,
                                                           String[] configResources,
                                                           AstXmlParserSupplier parserSupplier,
                                                           Set<ExtensionModel> extensions,
                                                           boolean disableValidations,
                                                           ZernovaContext muleContext,
                                                           ExpressionLanguageMetadataService expressionLanguageMetadataService)
      throws ConfigurationException {
    return parseAndBuildAppExtensionModel(artifactName, configResources,
                                          parserSupplier, extensions, disableValidations,
                                          muleContext.getExecutionClassLoader(), muleContext.getConfiguration(),
                                          expressionLanguageMetadataService);
  }

  /**
   * Parses {@code configResources} for a Mule application and returns an {@link ArtifactAst} enriched with an additional
   * {@link ExtensionModel} which models the app itself, with all its defined operations, sources, functions, etc.
   * <p>
   * This extra {@link ExtensionModel} is accessible through the {@link ArtifactAst#dependencies()} set its named after the
   * {@code muleContext.getConfiguration.getId()} return value
   *
   * @param artifactName                      the name of the artifact whose configs will be parsed
   * @param configResources                   the paths to the application's config files
   * @param parserSupplier                    the supplier used to obtain the ast parser. It might be invoked several times during
   *                                          the parsing
   * @param extensions                        the initial set of extensions the app depends on.
   * @param artifactType                      the artifact type
   * @param disableValidations                whether to disable DSL validation
   * @param artifactClassLoader               the artifact's classloader
   * @param muleConfiguration                 the application's muleConfiguration
   * @param expressionLanguageMetadataService the {@link ExpressionLanguageMetadataService} used to resolve types.
   * @return an {@link ArtifactAst}
   * @throws ConfigurationException it the app couldn't be parsed
   *
   * @since 4.9
   */
  public static ArtifactAst parseAndBuildAppExtensionModel(String artifactName,
                                                           String[] configResources,
                                                           AstXmlParserSupplier parserSupplier,
                                                           Set<ExtensionModel> extensions,
                                                           boolean disableValidations,
                                                           ClassLoader artifactClassLoader,
                                                           ZernovaConfiguration muleConfiguration,
                                                           ExpressionLanguageMetadataService expressionLanguageMetadataService)
      throws ConfigurationException {
    return parseAndBuildAppExtensionModel(artifactName,
                                          configResources,
                                          parserSupplier,
                                          extensions,
                                          disableValidations,
                                          artifactClassLoader,
                                          muleConfiguration.getArtifactCoordinates(),
                                          expressionLanguageMetadataService);
  }

  /**
   * Parses {@code configResources} for a Mule application and returns an {@link ArtifactAst} enriched with an additional
   * {@link ExtensionModel} which models the app itself, with all its defined operations, sources, functions, etc.
   * <p>
   * This extra {@link ExtensionModel} is accessible through the {@link ArtifactAst#dependencies()} set its named after the
   * {@code muleContext.getConfiguration.getId()} return value
   *
   * @param artifactName                      the name of the artifact whose configs will be parsed
   * @param configResources                   the paths to the application's config files
   * @param parserSupplier                    the supplier used to obtain the ast parser. It might be invoked several times during
   *                                          the parsing
   * @param extensions                        the initial set of extensions the app depends on.
   * @param artifactType                      the artifact type
   * @param disableValidations                whether to disable DSL validation
   * @param artifactClassLoader               the artifact's classloader
   * @param artifactCoordinates               the application's Maven coordinates
   * @param expressionLanguageMetadataService the {@link ExpressionLanguageMetadataService} used to resolve types.
   * @return an {@link ArtifactAst}
   * @throws ConfigurationException it the app couldn't be parsed
   *
   * @since 4.10
   */
  public static ArtifactAst parseAndBuildAppExtensionModel(String artifactName,
                                                           String[] configResources,
                                                           AstXmlParserSupplier parserSupplier,
                                                           Set<ExtensionModel> extensions,
                                                           boolean disableValidations,
                                                           ClassLoader artifactClassLoader,
                                                           Optional<ArtifactCoordinates> artifactCoordinates,
                                                           ExpressionLanguageMetadataService expressionLanguageMetadataService)
      throws ConfigurationException {
    return parseArtifact(artifactName,
                         left(loadConfigResources(configResources, artifactClassLoader)),
                         parserSupplier,
                         extensions,
                         disableValidations,
                         artifactClassLoader,
                         getExtensionModelLoadingMediator(artifactName,
                                                          artifactCoordinates,
                                                          expressionLanguageMetadataService));
  }

  /**
   * Parses {@code configResources} for a Mule application and returns an {@link ArtifactAst} enriched with an additional
   * {@link ExtensionModel} which models the app itself, with all its defined operations, sources, functions, etc.
   * <p>
   * This extra {@link ExtensionModel} is accessible through the {@link ArtifactAst#dependencies()} set its named after the
   * {@code muleContext.getConfiguration.getId()} return value
   *
   * @param artifactName                      the name of the artifact whose configs will be parsed
   * @param configResources                   pointers to the application's config files
   * @param parserSupplier                    the supplier used to obtain the ast parser. It might be invoked several times during
   *                                          the parsing
   * @param extensions                        the initial set of extensions the app depends on.
   * @param artifactType                      the artifact type
   * @param disableValidations                whether to disable DSL validation
   * @param artifactClassLoader               the artifact's classloader
   * @param muleConfiguration                 the application's muleConfiguration
   * @param expressionLanguageMetadataService the {@link ExpressionLanguageMetadataService} used to resolve types.
   * @return an {@link ArtifactAst}
   * @throws ConfigurationException it the app couldn't be parsed
   *
   * @since 4.9
   */
  public static ArtifactAst parseAndBuildAppExtensionModel(String artifactName,
                                                           ConfigResource[] configResources,
                                                           AstXmlParserSupplier parserSupplier,
                                                           Set<ExtensionModel> extensions,
                                                           boolean disableValidations,
                                                           ClassLoader artifactClassLoader,
                                                           ZernovaConfiguration muleConfiguration,
                                                           ExpressionLanguageMetadataService expressionLanguageMetadataService)
      throws ConfigurationException {
    return parseArtifact(artifactName,
                         left(configResources),
                         parserSupplier,
                         extensions,
                         disableValidations,
                         artifactClassLoader,
                         getExtensionModelLoadingMediator(artifactName,
                                                          muleConfiguration.getArtifactCoordinates(),
                                                          expressionLanguageMetadataService));
  }

  /**
   * Processes {@code appXmlConfigDocuments} for a Mule application and returns an {@link ArtifactAst} enriched with an additional
   * {@link ExtensionModel} which models the app itself, with all its defined operations, sources, functions, etc.
   * <p>
   * This extra {@link ExtensionModel} is accessible through the {@link ArtifactAst#dependencies()} set its named after the
   * {@code muleContext.getConfiguration.getId()} return value
   *
   * @param artifactName                      the name of the artifact whose configs will be parsed
   * @param appXmlConfigDocuments             the parsed XML DOMs for the config files
   * @param parserSupplier                    the supplier used to obtain the ast parser. It might be invoked several times during
   *                                          the parsing
   * @param extensions                        the initial set of extensions the app depends on.
   * @param artifactType                      the artifact type
   * @param disableValidations                whether to disable DSL validation
   * @param muleContext                       the app's {@link ZernovaContext}
   * @param expressionLanguageMetadataService the {@link ExpressionLanguageMetadataService} used to resolve types.
   * @return an {@link ArtifactAst}
   * @throws ConfigurationException it the app couldn't be parsed
   *
   * @since 4.8
   * @deprecated Use
   *             {@link #parseAndBuildAppExtensionModel(String, Map, AstXmlParserSupplier, Set, boolean, ClassLoader, ZernovaConfiguration, ExpressionLanguageMetadataService)}
   *             instead.
   */
  @Deprecated
  public static ArtifactAst parseAndBuildAppExtensionModel(String artifactName,
                                                           Map<String, Document> appXmlConfigDocuments,
                                                           AstXmlParserSupplier parserSupplier,
                                                           Set<ExtensionModel> extensions,
                                                           boolean disableValidations,
                                                           ZernovaContext muleContext,
                                                           ExpressionLanguageMetadataService expressionLanguageMetadataService)
      throws ConfigurationException {
    return parseAndBuildAppExtensionModel(artifactName, appXmlConfigDocuments,
                                          parserSupplier, extensions, disableValidations,
                                          muleContext.getExecutionClassLoader(), muleContext.getConfiguration(),
                                          expressionLanguageMetadataService);
  }

  /**
   * Processes {@code appXmlConfigDocuments} for a Mule application and returns an {@link ArtifactAst} enriched with an additional
   * {@link ExtensionModel} which models the app itself, with all its defined operations, sources, functions, etc.
   * <p>
   * This extra {@link ExtensionModel} is accessible through the {@link ArtifactAst#dependencies()} set its named after the
   * {@code muleContext.getConfiguration.getId()} return value
   *
   * @param artifactName                      the name of the artifact whose configs will be parsed
   * @param appXmlConfigDocuments             the parsed XML DOMs for the config files
   * @param parserSupplier                    the supplier used to obtain the ast parser. It might be invoked several times during
   *                                          the parsing
   * @param extensions                        the initial set of extensions the app depends on.
   * @param artifactType                      the artifact type
   * @param disableValidations                whether to disable DSL validation
   * @param artifactClassLoader               the artifact's classloader
   * @param muleConfiguration                 the application's muleConfiguration
   * @param expressionLanguageMetadataService the {@link ExpressionLanguageMetadataService} used to resolve types.
   * @return an {@link ArtifactAst}
   * @throws ConfigurationException it the app couldn't be parsed
   *
   * @since 4.9
   */
  public static ArtifactAst parseAndBuildAppExtensionModel(String artifactName,
                                                           Map<String, Document> appXmlConfigDocuments,
                                                           AstXmlParserSupplier parserSupplier,
                                                           Set<ExtensionModel> extensions,
                                                           boolean disableValidations,
                                                           ClassLoader artifactClassLoader,
                                                           ZernovaConfiguration muleConfiguration,
                                                           ExpressionLanguageMetadataService expressionLanguageMetadataService)
      throws ConfigurationException {
    return parseArtifact(artifactName,
                         right(appXmlConfigDocuments),
                         parserSupplier,
                         extensions,
                         disableValidations,
                         artifactClassLoader,
                         getExtensionModelLoadingMediator(artifactName,
                                                          muleConfiguration.getArtifactCoordinates(),
                                                          expressionLanguageMetadataService));
  }

  /**
   * If the {@code ast} represents an application which defines reusable components (operations, sources, etc), it returns an
   * {@link ExtensionModel} which represents it.
   *
   * @param ast                               the application's AST
   * @param artifactClassLoader               the application's classloader
   * @param muleContext                       the application's context
   * @param expressionLanguageMetadataService the {@link ExpressionLanguageMetadataService} used to resolve types.
   * @return an optional {@link ExtensionModel}
   * @deprecated Use
   *             {@link #parseArtifactExtensionModel(ArtifactAst, ClassLoader, ZernovaConfiguration, ExtensionManager, ExpressionLanguageMetadataService)}
   *             instead.
   */
  @Deprecated
  public static Optional<ExtensionModel> parseArtifactExtensionModel(ArtifactAst ast,
                                                                     ClassLoader artifactClassLoader,
                                                                     ZernovaContext muleContext,
                                                                     ExpressionLanguageMetadataService expressionLanguageMetadataService)
      throws ConfigurationException {
    return parseArtifactExtensionModel(ast, artifactClassLoader,
                                       muleContext.getConfiguration(),
                                       muleContext.getExtensionManager(),
                                       expressionLanguageMetadataService);
  }

  /**
   * If the {@code ast} represents an application which defines reusable components (operations, sources, etc), it returns an
   * {@link ExtensionModel} which represents it.
   *
   * @param ast                               the application's AST
   * @param artifactClassLoader               the application's classloader
   * @param muleConfiguration                 the application's muleConfiguration
   * @param extensionManager                  the application's extensionManager
   * @param expressionLanguageMetadataService the {@link ExpressionLanguageMetadataService} used to resolve types.
   * @return an optional {@link ExtensionModel}
   * @since 4.9
   */
  public static Optional<ExtensionModel> parseArtifactExtensionModel(ArtifactAst ast,
                                                                     ClassLoader artifactClassLoader,
                                                                     ZernovaConfiguration muleConfiguration,
                                                                     ExtensionManager extensionManager,
                                                                     ExpressionLanguageMetadataService expressionLanguageMetadataService)
      throws ConfigurationException {
    return getExtensionModelLoadingMediator(muleConfiguration.getId(), muleConfiguration.getArtifactCoordinates(),
                                            expressionLanguageMetadataService)
                                                .loadExtensionModel(ast, artifactClassLoader, extensionManager.getExtensions());
  }

  private static ZernovaSdkExtensionModelLoadingMediator getExtensionModelLoadingMediator(String artifactId,
                                                                                       Optional<ArtifactCoordinates> artifactCoordinates,
                                                                                       ExpressionLanguageMetadataService expressionLanguageMetadataService) {
    return new ZernovaSdkApplicationExtensionModelLoadingMediator(expressionLanguageMetadataService, artifactId,
                                                               artifactCoordinates,
                                                               empty());
  }

  private ArtifactAstUtils() {}
}
