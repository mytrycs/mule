/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.delegate;

import static com.zernova.runtime.module.extension.internal.loader.parser.java.utils.MinZernovaVersionUtils.declarerWithMmv;
import static com.zernova.runtime.module.extension.internal.loader.utils.ModelLoaderUtils.addSemanticTerms;

import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.FunctionDeclarer;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.parser.FunctionModelParser;
import com.zernova.runtime.module.extension.internal.loader.java.property.FunctionExecutorModelProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for declaring functions through a {@link DefaultExtensionModelLoaderDelegate}
 *
 * @since 4.0
 */
final class FunctionModelLoaderDelegate extends AbstractComponentModelLoaderDelegate {

  private final Map<FunctionModelParser, FunctionDeclarer> functionDeclarers = new HashMap<>();

  FunctionModelLoaderDelegate(DefaultExtensionModelLoaderDelegate delegate) {
    super(delegate);
  }

  void declareFunctions(ExtensionDeclarer extensionDeclarer, List<FunctionModelParser> parsers, ExtensionLoadingContext context) {

    for (FunctionModelParser parser : parsers) {

      if (parser.isIgnored()) {
        continue;
      }

      if (functionDeclarers.containsKey(parser)) {
        extensionDeclarer.withFunction(functionDeclarers.get(parser));
        continue;
      }

      final FunctionDeclarer function = extensionDeclarer.withFunction(parser.getName())
          .describedAs(parser.getDescription());

      parser.getDeprecationModel().ifPresent(function::withDeprecation);

      parser.getFunctionExecutorFactory()
          .map(FunctionExecutorModelProperty::new)
          .ifPresent(function::withModelProperty);

      parser.getOutputType().applyOn(function.withOutput());
      loader.getParameterModelsLoaderDelegate().declare(function, parser.getParameterGroupModelParsers(), context);
      parser.getAdditionalModelProperties().forEach(function::withModelProperty);
      if (context.isResolveMinZernovaVersion()) {
        parser.getResolvedMinZernovaVersion().ifPresent(resolvedMMV -> declarerWithMmv(function, resolvedMMV));
      }
      addSemanticTerms(function.getDeclaration(), parser);

      functionDeclarers.put(parser, function);
    }
  }
}
