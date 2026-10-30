/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.ast.api.validation.Validation.Level.ERROR;
import static com.zernova.runtime.ast.api.validation.ValidationResultItem.create;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * AST validation checking that all components belong to some extension model.
 *
 * @since 4.5.0
 */
public class AllComponentsBelongToSomeExtensionModel implements Validation {

  @Override
  public String getName() {
    return "All components belong to some extension model";
  }

  @Override
  public String getDescription() {
    return "All components belong to some extension model";
  }

  @Override
  public Level getLevel() {
    return ERROR;
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    // This validation should be applied to all components.
    return c -> true;
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst component, ArtifactAst artifact) {
    if (component.getExtensionModel() == null) {
      return of(create(component, this,
                       format("The component '%s' doesn't belong to any extension model", component.getIdentifier())));
    }
    return empty();
  }
}
