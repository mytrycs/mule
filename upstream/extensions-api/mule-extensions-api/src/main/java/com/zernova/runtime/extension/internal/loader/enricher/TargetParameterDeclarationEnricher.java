/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.enricher;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.runtime.api.meta.ExpressionSupport.REQUIRED;
import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.OUTPUT;
import static com.zernova.runtime.api.meta.model.parameter.ParameterRole.BEHAVIOUR;
import static com.zernova.runtime.extension.api.ExtensionConstants.TARGET_PARAMETER_DESCRIPTION;
import static com.zernova.runtime.extension.api.ExtensionConstants.TARGET_PARAMETER_DISPLAY_NAME;
import static com.zernova.runtime.extension.api.ExtensionConstants.TARGET_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.ExtensionConstants.TARGET_VALUE_PARAMETER_DESCRIPTION;
import static com.zernova.runtime.extension.api.ExtensionConstants.TARGET_VALUE_PARAMETER_DISPLAY_NAME;
import static com.zernova.runtime.extension.api.ExtensionConstants.TARGET_VALUE_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.annotation.param.Optional.PAYLOAD;
import static com.zernova.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;
import static com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase.STRUCTURE;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.of;

import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.VoidType;
import com.zernova.metadata.api.model.impl.DefaultStringType;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.display.LayoutModel;
import com.zernova.runtime.api.util.collection.SmallMap;
import com.zernova.runtime.extension.api.ExtensionConstants;
import com.zernova.runtime.extension.api.exception.IllegalOperationModelDefinitionException;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.IdempotentDeclarationEnricherWalkDelegate;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;
import com.zernova.runtime.extension.internal.property.TargetModelProperty;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link DeclarationEnricher} which adds a {@link ExtensionConstants#TARGET_PARAMETER_NAME} parameter to all non void
 * operations
 *
 * @since 1.0
 */
public final class TargetParameterDeclarationEnricher implements WalkingDeclarationEnricher {

  private static final DefaultStringType stringType = BaseTypeBuilder.create(JAVA).stringType().build();

  /**
   * This map holds as key the names of the extensions that have Operations that will not be enriched and as value a {@link Set}
   * with the names of the Operations.
   */
  private static final Map<String, Set<String>> BLOCK_LIST;

  static {
    Map<String, Set<String>> block = new SmallMap<>();
    block.put("ee", singleton("transform"));

    BLOCK_LIST = unmodifiableMap(block);
  }

  @Override
  public DeclarationEnricherPhase getExecutionPhase() {
    return STRUCTURE;
  }

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    String extensionName = extensionLoadingContext.getExtensionDeclarer().getDeclaration().getName();
    return of(new WalkDelegateDelegateDeclaration(BLOCK_LIST.getOrDefault(extensionName, emptySet())));
  }

  private static class WalkDelegateDelegateDeclaration extends IdempotentDeclarationEnricherWalkDelegate {

    private final Set<String> blockedOperationsNames;
    private final MetadataType attributeType;
    private final MetadataType targetValue;

    private WalkDelegateDelegateDeclaration(Set<String> blockedOperationsNames) {
      this.attributeType = stringType;
      this.targetValue = stringType;
      this.blockedOperationsNames = blockedOperationsNames;
    }

    @Override
    protected void onOperation(OperationDeclaration declaration) {
      if (blockedOperationsNames.contains(declaration.getName())) {
        return;
      }
      final MetadataType outputType = declaration.getOutput().getType();
      if (outputType == null) {
        throw new IllegalOperationModelDefinitionException(format("Operation '%s' does not specify an output type",
                                                                  declaration.getName()));
      }
      if (!(outputType instanceof VoidType)) {
        enrichWithTargetParameterDeclaration(declaration);
        enrichWithTargetValueParameterDeclaration(declaration);
      }
    }

    private void enrichWithTargetParameterDeclaration(OperationDeclaration operationDeclaration) {
      if (!definesTargetParameter(operationDeclaration)) {
        ParameterDeclaration targetParameterDeclaration = new ParameterDeclaration(TARGET_PARAMETER_NAME);
        targetParameterDeclaration.setDescription(TARGET_PARAMETER_DESCRIPTION);
        targetParameterDeclaration.setExpressionSupport(NOT_SUPPORTED);
        targetParameterDeclaration.setRequired(false);
        targetParameterDeclaration.setParameterRole(BEHAVIOUR);
        targetParameterDeclaration.setType(attributeType, false);
        targetParameterDeclaration.setDisplayModel(DisplayModel.builder().displayName(TARGET_PARAMETER_DISPLAY_NAME).build());
        targetParameterDeclaration.setLayoutModel(LayoutModel.builder().tabName(ADVANCED_TAB).build());
        addTargetParameter(operationDeclaration, targetParameterDeclaration);
      }
    }

    private void enrichWithTargetValueParameterDeclaration(OperationDeclaration operationDeclaration) {
      if (!definesTargetValueParameter(operationDeclaration)) {
        ParameterDeclaration targetValueParameterDeclaration = new ParameterDeclaration(TARGET_VALUE_PARAMETER_NAME);
        targetValueParameterDeclaration.setDescription(TARGET_VALUE_PARAMETER_DESCRIPTION);
        targetValueParameterDeclaration.setExpressionSupport(REQUIRED);
        targetValueParameterDeclaration.setRequired(false);
        targetValueParameterDeclaration.setDefaultValue(PAYLOAD);
        targetValueParameterDeclaration.setParameterRole(BEHAVIOUR);
        targetValueParameterDeclaration.setType(targetValue, false);
        targetValueParameterDeclaration
            .setDisplayModel(DisplayModel.builder().displayName(TARGET_VALUE_PARAMETER_DISPLAY_NAME).build());
        targetValueParameterDeclaration.setLayoutModel(LayoutModel.builder().tabName(ADVANCED_TAB).build());
        targetValueParameterDeclaration.addModelProperty(new TargetModelProperty());
        addTargetValueParameter(operationDeclaration, targetValueParameterDeclaration);
      }
    }
  }

  private static void addTargetValueParameter(OperationDeclaration operationDeclaration,
                                              ParameterDeclaration targetValueParameter) {
    operationDeclaration.getParameterGroup(OUTPUT).addParameter(targetValueParameter);
  }

  private static void addTargetParameter(OperationDeclaration operationDeclaration, ParameterDeclaration targetParameter) {
    operationDeclaration.getParameterGroup(OUTPUT).addParameter(targetParameter);
  }

  private static boolean definesTargetValueParameter(OperationDeclaration operationDeclaration) {
    return operationDeclaration.getParameterGroup(OUTPUT).getParameters().stream()
        .anyMatch(parameterDeclaration -> parameterDeclaration.getName().equals(TARGET_VALUE_PARAMETER_NAME));
  }

  private static boolean definesTargetParameter(OperationDeclaration operationDeclaration) {
    return operationDeclaration.getParameterGroup(OUTPUT).getParameters().stream()
        .anyMatch(parameterDeclaration -> parameterDeclaration.getName().equals(TARGET_PARAMETER_NAME));
  }
}
