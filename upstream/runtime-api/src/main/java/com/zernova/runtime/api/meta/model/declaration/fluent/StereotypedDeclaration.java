/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.declaration.fluent;

import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link NamedDeclaration} which adds a {@link List} of {@link ParameterDeclaration}
 *
 * @param <T> the concrete type for {@code this} declaration
 * @since 1.0
 */
public abstract class StereotypedDeclaration<T extends StereotypedDeclaration>
    extends ParameterizedWithMinZernovaVersionDeclaration<T>
    implements WithStereotypesDeclaration, WithDeprecatedDeclaration {

  private StereotypeModel stereotype;
  private DeprecationModel deprecation;

  /**
   * {@inheritDoc}
   */
  StereotypedDeclaration(String name) {
    super(name);
  }


  public StereotypeModel getStereotype() {
    return stereotype;
  }

  public void withStereotype(StereotypeModel stereotype) {
    this.stereotype = stereotype;
  }

  @Override
  public Optional<DeprecationModel> getDeprecation() {
    return ofNullable(deprecation);
  }

  @Override
  public void withDeprecation(DeprecationModel deprecation) {
    this.deprecation = deprecation;
  }
}
