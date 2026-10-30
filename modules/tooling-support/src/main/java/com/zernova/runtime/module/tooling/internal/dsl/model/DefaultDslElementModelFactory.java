/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.dsl.model;

import static java.util.stream.Collectors.toMap;

import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.app.declaration.api.ElementDeclaration;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.config.api.dsl.model.DslElementModelFactory;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.metadata.api.dsl.DslElementModel;

import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of a {@link DslElementModelFactory}
 *
 * @since 4.0
 */
public class DefaultDslElementModelFactory implements DslElementModelFactory {

  private DeclarationBasedElementModelFactory declarationBasedDelegate;
  private ComponentAstBasedElementModelFactory componentAstBasedDelegate;

  public void setContext(DslResolvingContext context) {
    final Map<ExtensionModel, DslSyntaxResolver> resolvers = context.getExtensions().stream()
        .collect(toMap(e -> e, e -> DslSyntaxResolver.getDefault(e, context)));

    this.declarationBasedDelegate = new DeclarationBasedElementModelFactory(context, resolvers);
    this.componentAstBasedDelegate = new ComponentAstBasedElementModelFactory();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> Optional<DslElementModel<T>> create(ElementDeclaration componentDeclaration) {
    return declarationBasedDelegate.create(componentDeclaration);
  }

  @Override
  public <T> Optional<DslElementModel<T>> create(ComponentAst componentModel) {
    return componentAstBasedDelegate.create(componentModel);
  }
}
