/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.stereotypes;

import static java.util.Optional.ofNullable;
import static com.zernova.runtime.api.meta.model.stereotype.StereotypeModelBuilder.newStereotype;
import static com.zernova.runtime.api.util.FunctionalUtils.computeIfAbsent;
import static com.zernova.runtime.core.api.util.StringUtils.isBlank;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.PROCESSOR;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.SOURCE;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.VALIDATOR_DEFINITION;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.stereotypes.JavaStereotypeModelParserUtils.asDefinition;
import static com.zernova.sdk.api.stereotype.ZernovaStereotypes.VALIDATOR;

import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModelBuilder;
import com.zernova.runtime.extension.api.loader.parser.StereotypeModelFactory;
import com.zernova.sdk.api.stereotype.StereotypeDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation for {@link StereotypeModelFactory}
 *
 * @since 4.5.0
 */
public class DefaultStereotypeModelFactory implements StereotypeModelFactory {


  private final Map<StereotypeDefinition, StereotypeModel> stereotypesCache = new HashMap<>();

  private String namespace;
  private StereotypeModel sourceParent;
  private StereotypeModel processorParent;
  private StereotypeModel validatorStereotype;

  @Override
  public StereotypeModel createStereotype(StereotypeDefinition stereotypeDefinition) {
    return createStereotype(stereotypeDefinition, namespace);
  }

  @Override
  public StereotypeModel createStereotype(StereotypeDefinition stereotypeDefinition, String namespace) {
    return computeIfAbsent(stereotypesCache, stereotypeDefinition, definition -> {
      String resolvedNamespace = isBlank(stereotypeDefinition.getNamespace()) ? namespace : stereotypeDefinition.getNamespace();
      final StereotypeModelBuilder builder = newStereotype(stereotypeDefinition.getName(), resolvedNamespace);
      stereotypeDefinition.getParent().ifPresent(parent -> {
        String parentNamespace = parent.getNamespace();
        if (isBlank(parentNamespace)) {
          parentNamespace = namespace;
        }
        builder.withParent(createStereotype(parent, parentNamespace));
      });

      return builder.build();
    });
  }

  @Override
  public StereotypeModel createStereotype(String name, StereotypeModel parent) {
    return createStereotype(name, namespace, parent);
  }

  @Override
  public StereotypeModel createStereotype(String name, String namespace, StereotypeModel parent) {
    final String effectiveNamespace = namespace.toUpperCase();
    return createStereotype(new StereotypeDefinition() {

      @Override
      public String getName() {
        return name;
      }

      @Override
      public String getNamespace() {
        return effectiveNamespace;
      }

      @Override
      public Optional<StereotypeDefinition> getParent() {
        return ofNullable(parent).map(p -> asDefinition(p));
      }
    });
  }

  @Override
  public StereotypeModel getProcessorParentStereotype() {
    return processorParent;
  }

  @Override
  public StereotypeModel getSourceParentStereotype() {
    return sourceParent;
  }

  @Override
  public StereotypeModel getValidatorStereotype() {
    return validatorStereotype;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
    processorParent = newStereotype(PROCESSOR.getType(), namespace).withParent(PROCESSOR).build();
    sourceParent = newStereotype(SOURCE.getType(), namespace).withParent(SOURCE).build();
    validatorStereotype = newStereotype(VALIDATOR_DEFINITION.getName(), namespace)
        .withParent(VALIDATOR)
        .build();
  }
}
