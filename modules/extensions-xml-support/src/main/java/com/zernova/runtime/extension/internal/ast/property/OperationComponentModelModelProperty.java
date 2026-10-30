/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.ast.property;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.core.api.processor.Processor;

/**
 * Property to store the <operation/>'s {@link ComponentAst} and the <body/> that are contained in an extension written in XML.
 *
 * @since 4.0
 */
public class OperationComponentModelModelProperty implements ModelProperty {

  private static final long serialVersionUID = 3874939115159350906L;

  private final ComponentAst operationComponentModel;
  private final ComponentAst bodyComponentModel;

  /**
   * Constructs a {@link ModelProperty} that will hold the complete <operation/> and its {@link Processor}s defined in a <body/>
   * element to be later macro expanded into a Mule application.
   *
   * @param operationComponentModel <operation/> element represented through {@link ComponentAst}s.
   * @param bodyComponentModel      <body/> element with all the {@link Processor} represented through {@link ComponentAst}s.
   */
  public OperationComponentModelModelProperty(ComponentAst operationComponentModel, ComponentAst bodyComponentModel) {
    this.operationComponentModel = operationComponentModel;
    this.bodyComponentModel = bodyComponentModel;
  }

  /**
   * @return the {@link ComponentAst} that's pointing to the <operation/> element
   */
  public ComponentAst getOperationComponentModel() {
    return operationComponentModel;
  }

  /**
   * @return the {@link ComponentAst} that's pointing to the <body/> element
   */
  public ComponentAst getBodyComponentModel() {
    return bodyComponentModel;
  }

  @Override
  public String getName() {
    return "componentModelModelProperty";
  }

  @Override
  public boolean isPublic() {
    return false;
  }
}
