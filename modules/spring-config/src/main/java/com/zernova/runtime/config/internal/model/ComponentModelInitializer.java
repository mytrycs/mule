/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.model;

import com.zernova.runtime.ast.api.ComponentAst;

import java.util.function.Predicate;

/**
 * Internal class to be used to initialize {@link ComponentAst} lazily.
 *
 * @since 4.1
 */
public interface ComponentModelInitializer {

  /**
   * Initializes the {@link <ComponentAst> componentModels} that match for the predicate.
   *
   * @param componentModelPredicate a {@link Predicate} for {@link ComponentAst} to be initialized.
   * @param applyStartPhase         boolean indicating if the Start phase should be applied to the created components
   */
  void initializeComponents(Predicate<ComponentAst> componentModelPredicate, boolean applyStartPhase);

}
