/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.type.catalog;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.metadata.message.api.el.ModuleDefinition.builder;

import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.builder.FunctionTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.message.api.el.ModuleDefinition;
import com.zernova.metadata.message.api.el.ModuleDefinition.Builder;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.function.FunctionModel;

import java.util.function.Function;

/**
 * Utility object to create a {@link ModuleDefinition} starting from a {@link ExtensionModel}. The resulting
 * {@link ModuleDefinition} object will have the functions and types from the original {@link ExtensionModel}.
 *
 * @since 4.5.0
 */
public class ExtensionModelToModuleDefinitionTransformer implements Function<ExtensionModel, ModuleDefinition> {

  @Override
  public ModuleDefinition apply(ExtensionModel extensionModel) {
    Builder moduleDefBuilder = builder(getPrefix(extensionModel));
    extensionModel.getFunctionModels().forEach(functionModel -> addFunction(functionModel, moduleDefBuilder));
    extensionModel.getTypes().forEach(type -> addType(type, moduleDefBuilder));
    return moduleDefBuilder.build();
  }

  private static String getPrefix(ExtensionModel extensionModel) {
    return extensionModel.getXmlDslModel().getPrefix();
  }

  private static void addType(MetadataType type, Builder moduleDefBuilder) {
    moduleDefBuilder.addType(type);
  }

  private static void addFunction(FunctionModel functionModel, Builder moduleDefBuilder) {
    FunctionTypeBuilder functionTypeBuilder = new BaseTypeBuilder(JAVA).functionType();
    functionModel.getAllParameterModels().forEach(parameterModel -> {
      if (parameterModel.isRequired()) {
        functionTypeBuilder.addOptionalParameterOf(parameterModel.getName(), parameterModel.getType());
      } else {
        functionTypeBuilder.addParameterOf(parameterModel.getName(), parameterModel.getType());
      }
    });

    functionTypeBuilder.returnType(functionModel.getOutput().getType());
    moduleDefBuilder.addElement(functionModel.getName(), functionTypeBuilder.build());
  }
}
