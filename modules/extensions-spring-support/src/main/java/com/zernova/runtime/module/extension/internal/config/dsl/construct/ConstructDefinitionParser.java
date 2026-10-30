/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.construct;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.construct.ConstructModel;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionDefinitionParser;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;
import com.zernova.runtime.module.extension.internal.parser.AbstractComponentDefinitionParser;
import com.zernova.runtime.module.extension.internal.runtime.operation.ConstructMessageProcessor;

import java.util.Optional;

/**
 * A {@link ExtensionDefinitionParser} for parsing {@link ConstructMessageProcessor} instances through a
 * {@link ConstructMessageProcessorObjectFactory}
 *
 * @since 4.0
 */
public class ConstructDefinitionParser extends AbstractComponentDefinitionParser<ConstructModel> {

  public ConstructDefinitionParser(Builder definition, ExtensionModel extensionModel,
                                   ConstructModel constructModel,
                                   DslSyntaxResolver dslSyntaxResolver,
                                   ExtensionParsingContext parsingContext,
                                   Optional<ClassTypeLoader> typeLoader) {
    super(definition, extensionModel, constructModel, dslSyntaxResolver, parsingContext, typeLoader);
  }

  @Override
  protected Class<ConstructMessageProcessor> getMessageProcessorType() {
    return ConstructMessageProcessor.class;
  }

  @Override
  protected Class<ConstructMessageProcessorObjectFactory> getMessageProcessorFactoryType() {
    return ConstructMessageProcessorObjectFactory.class;
  }
}
