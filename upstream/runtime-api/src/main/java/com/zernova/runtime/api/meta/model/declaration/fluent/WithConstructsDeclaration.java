/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.declaration.fluent;

import com.zernova.api.annotation.NoImplement;

import java.util.List;

/**
 * Contract interface for a {@link BaseDeclaration} in which it's possible to add/get {@link OperationDeclaration} objects
 *
 * @param <T> the generic type of the {@link BaseDeclaration} which is implementing the interface
 * @since 1.0
 */
@NoImplement
public interface WithConstructsDeclaration<T extends BaseDeclaration> {

  /**
   * @return an unmodifiable {@link List} with the available {@link ConstructDeclaration}s
   */
  List<ConstructDeclaration> getConstructs();

  /**
   * Adds a {@link ConstructDeclaration}
   *
   * @param declaration the declaration to be added
   * @return {@code this} declarer
   */
  T addConstruct(ConstructDeclaration declaration);

}
