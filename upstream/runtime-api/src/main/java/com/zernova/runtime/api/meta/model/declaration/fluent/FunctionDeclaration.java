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
import com.zernova.runtime.api.meta.model.operation.OperationModel;

import java.util.Optional;

/**
 * A declaration object for a {@link OperationModel}. It contains raw, unvalidated data which is used to declare the structure of
 * a {@link OperationModel}
 *
 * @since 1.0
 */
public class FunctionDeclaration extends ParameterizedWithMinZernovaVersionDeclaration<FunctionDeclaration>
    implements WithDeprecatedDeclaration {

  private OutputDeclaration output;
  private DeprecationModel deprecation;

  /**
   * {@inheritDoc}
   */
  public FunctionDeclaration(String name) {
    super(name);
  }

  public OutputDeclaration getOutput() {
    return output;
  }

  public void setOutput(OutputDeclaration output) {
    this.output = output;
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
