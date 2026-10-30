/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.operation;

import static com.zernova.runtime.extension.privileged.util.ComponentDeclarationUtils.isNoErrorMapping;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.model.VoidType;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionDefinitionParser;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;
import com.zernova.runtime.module.extension.internal.parser.AbstractComponentDefinitionParser;
import com.zernova.runtime.module.extension.internal.runtime.operation.OperationMessageProcessor;

import java.util.Optional;

/**
 * A {@link ExtensionDefinitionParser} for parsing {@link OperationMessageProcessor} instances through a
 * {@link OperationMessageProcessorObjectFactory}
 *
 * @since 4.0
 */
public class OperationDefinitionParser extends AbstractComponentDefinitionParser<OperationModel> {

  public OperationDefinitionParser(Builder definition, ExtensionModel extensionModel,
                                   OperationModel operationModel,
                                   DslSyntaxResolver dslSyntaxResolver,
                                   ExtensionParsingContext parsingContext,
                                   Optional<ClassTypeLoader> typeLoader) {
    super(definition, extensionModel, operationModel, dslSyntaxResolver, parsingContext, typeLoader);
  }

  @Override
  protected boolean hasErrorMappingsGroup() {
    return !isNoErrorMapping(getComponentModel());
  }

  @Override
  protected boolean hasOutputGroup() {
    return !(getComponentModel().getOutput().getType() instanceof VoidType);
  }

  @Override
  protected Class<OperationMessageProcessor> getMessageProcessorType() {
    return OperationMessageProcessor.class;
  }

  @Override
  protected Class<OperationMessageProcessorObjectFactory> getMessageProcessorFactoryType() {
    return OperationMessageProcessorObjectFactory.class;
  }
}
