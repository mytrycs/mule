/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.declaration.fluent;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.OutputModel;

/**
 * Allows configuring an {@link OutputDeclaration} through a fluent API
 *
 * @since 1.0
 */
public class OutputDeclarer<T extends OutputDeclarer>
    implements HasModelProperties<OutputDeclarer<T>>, HasType<OutputDeclarer<T>>, HasDynamicType<OutputDeclarer<T>> {

  private final OutputDeclaration declaration;

  OutputDeclarer(OutputDeclaration declaration) {
    this.declaration = declaration;
  }

  /**
   * Specifies that the {@link OutputModel} has a {@link MetadataType type} of <b>static</b> kind.
   *
   * @param type the type of the parameter
   * @return
   */
  @Override
  public T ofType(MetadataType type) {
    declaration.setType(type, false);
    return (T) this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T ofDynamicType(MetadataType type) {
    declaration.setType(type, true);
    return (T) this;
  }

  /**
   * Adds a description
   *
   * @param description a description
   * @return {@code this} descriptor
   */
  public T describedAs(String description) {
    declaration.setDescription(description);
    return (T) this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OutputDeclarer<T> withModelProperty(ModelProperty modelProperty) {
    declaration.addModelProperty(modelProperty);
    return this;
  }

  /**
   * Gets the declaration object for this descriptor
   *
   * @return a {@link OutputDeclaration}
   */
  public OutputDeclaration getDeclaration() {
    return declaration;
  }
}
