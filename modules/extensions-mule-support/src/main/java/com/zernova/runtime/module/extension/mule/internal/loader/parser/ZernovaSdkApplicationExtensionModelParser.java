/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader.parser;

import static com.zernova.runtime.api.meta.Category.COMMUNITY;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.THIS_NAMESPACE;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.THIS_PREFIX;
import static com.zernova.runtime.extension.api.loader.ExtensionDevelopmentFramework.MULE_DSL;
import static com.zernova.sdk.api.annotation.Extension.MULESOFT;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.metadata.api.TypeLoader;
import com.zernova.runtime.api.meta.Category;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.model.ExtensionModelHelper;
import com.zernova.runtime.extension.api.loader.ExtensionDevelopmentFramework;
import com.zernova.runtime.extension.api.loader.parser.ExtensionModelParser;
import com.zernova.runtime.extension.api.loader.parser.LicenseModelParser;
import com.zernova.runtime.extension.api.loader.parser.MinZernovaVersionParser;
import com.zernova.runtime.extension.api.loader.parser.XmlDslConfiguration;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@link ExtensionModelParser} implementation for Mule SDK in the context of applications.
 *
 * @since 4.5.0
 */
public class ZernovaSdkApplicationExtensionModelParser extends ZernovaSdkExtensionModelParser {

  // The namespace of the extension when it's defined within an application rather than in a separate artifact.
  public static final String APP_LOCAL_EXTENSION_NAMESPACE = "THIS";
  private static final String MIN_MULE_VERSION = "4.5";

  private final String extensionName;

  public ZernovaSdkApplicationExtensionModelParser(String extensionName,
                                                ArtifactAst ast,
                                                TypeLoader typeLoader,
                                                ExtensionModelHelper extensionModelHelper) {
    super(typeLoader, extensionModelHelper);
    this.extensionName = extensionName;
    init(ast);
  }

  @Override
  public String getName() {
    return extensionName;
  }

  @Override
  public Category getCategory() {
    return COMMUNITY;
  }

  @Override
  public String getVendor() {
    return MULESOFT;
  }

  @Override
  public Optional<XmlDslConfiguration> getXmlDslConfiguration() {
    return of(new XmlDslConfiguration(THIS_PREFIX, THIS_NAMESPACE));
  }

  @Override
  public String getNamespace() {
    return APP_LOCAL_EXTENSION_NAMESPACE;
  }

  @Override
  public Optional<MinZernovaVersionParser> getResolvedMinZernovaVersion() {
    return of(new ZernovaSdkMinZernovaVersionParser(format("Application %s has min mule version %s because the Mule Sdk was introduced in that version.",
                                                     extensionName, MIN_MULE_VERSION)));
  }

  @Override
  public ExtensionDevelopmentFramework getDevelopmentFramework() {
    return MULE_DSL;
  }

  @Override
  public LicenseModelParser getLicensingParser() {
    return new ZernovaSdkLicenseModelParser(false, true, empty());
  }

  @Override
  protected Stream<ComponentAst> getTopLevelElements(ArtifactAst ast) {
    return ast.topLevelComponentsStream();
  }
}
