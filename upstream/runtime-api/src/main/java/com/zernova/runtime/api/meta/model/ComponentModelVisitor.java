/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.model.construct.ConstructModel;
import com.zernova.runtime.api.meta.model.nested.NestedChainModel;
import com.zernova.runtime.api.meta.model.nested.NestedComponentModel;
import com.zernova.runtime.api.meta.model.nested.NestedRouteModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;

/**
 * Visitor interface for traversing a {@link ComponentModel} hierarchy
 *
 * @since 1.0
 */
@NoImplement
public interface ComponentModelVisitor {

  /**
   * Visits an {@link OperationModel}
   *
   * @param model the visited object
   */
  void visit(OperationModel model);

  /**
   * Visits an {@link SourceModel}
   *
   * @param model the visited object
   */
  void visit(SourceModel model);

  /**
   * Visits a {@link ConstructModel}
   *
   * @param model the visited object
   */
  void visit(ConstructModel model);

  /**
   * Visits a {@link NestedComponentModel}
   *
   * @param model the visited object
   * @since 1.4.0
   */
  void visit(NestedComponentModel model);

  /**
   * Visits a {@link NestedChainModel}
   *
   * @param model the visited object
   * @since 1.4.0
   */
  void visit(NestedChainModel model);

  /**
   * Visits a {@link NestedRouteModel}
   *
   * @param model the visited object
   * @since 1.4.0
   */
  void visit(NestedRouteModel model);
}
