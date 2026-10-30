/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader.parser;

import static com.zernova.runtime.extension.api.ExtensionConstants.MULE_SDK_ARTIFACT_AST_PROPERTY_NAME;
import static com.zernova.runtime.module.extension.mule.internal.loader.parser.utils.ZernovaSdkExtensionLoadingUtils.createExtensionModelHelper;
import static com.zernova.runtime.module.extension.mule.internal.loader.parser.utils.ZernovaSdkExtensionLoadingUtils.createTypeLoader;
import static com.zernova.runtime.module.extension.mule.internal.loader.parser.utils.ZernovaSdkExtensionLoadingUtils.getRequiredLoadingParameter;

import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.parser.ExtensionModelParser;
import com.zernova.runtime.extension.api.loader.parser.ExtensionModelParserFactory;

/**
 * {@link ExtensionModelParserFactory} implementation for Mule SDK in the context of plugins.
 *
 * Takes the artifact's AST as parameter of the {@link ExtensionLoadingContext} using {@link MULE_SDK_ARTIFACT_AST_PROPERTY_NAME}.
 *
 * @since 4.5.0
 */
public class ZernovaSdkPluginExtensionModelParserFactory implements ExtensionModelParserFactory {

  @Override
  public ExtensionModelParser createParser(ExtensionLoadingContext context) {
    ArtifactAst artifactAst = getRequiredLoadingParameter(context, MULE_SDK_ARTIFACT_AST_PROPERTY_NAME);
    return new ZernovaSdkPluginExtensionModelParser(artifactAst, createTypeLoader(context),
                                                 createExtensionModelHelper(context));
  }
}
