/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.api.util.NameValidationUtil.verifyStringDoesNotContainsReservedCharacters;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.topLevelElement;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.hasPropertyPlaceholder;
import static com.zernova.runtime.ast.api.validation.Validation.Level.ERROR;
import static com.zernova.runtime.ast.api.validation.ValidationResultItem.create;

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
 * 'name' attribute in top-level elements only have valid characters.
 */
public class NameHasValidCharacters implements Validation {

  @Override
  public String getName() {
    return "Names have valid characters";
  }

  @Override
  public String getDescription() {
    return "'name' attribute in top-level elements only have valid characters.";
  }

  @Override
  public Level getLevel() {
    return ERROR;
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return topLevelElement()
        .and(currentElemement(component -> component.getComponentId().isPresent()));
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst component, ArtifactAst artifact) {
    final String nameAttributeValue = component.getComponentId().get();

    try {
      verifyStringDoesNotContainsReservedCharacters(nameAttributeValue);
      return empty();
    } catch (IllegalArgumentException e) {
      return of(create(component, this,
                       "Invalid global element name '" + nameAttributeValue + "'. Problem is: " + e.getMessage(),
                       hasPropertyPlaceholder(nameAttributeValue)));
    }
  }
}
