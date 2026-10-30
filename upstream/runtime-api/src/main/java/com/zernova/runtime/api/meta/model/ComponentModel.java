/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.model.declaration.fluent.HasSemanticTerms;
import com.zernova.runtime.api.meta.model.deprecated.DeprecableModel;
import com.zernova.runtime.api.meta.model.display.HasDisplayModel;
import com.zernova.runtime.api.meta.model.error.ThrowsErrors;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.api.meta.model.stereotype.HasStereotypeModel;
import com.zernova.runtime.api.meta.model.version.HasMinZernovaVersion;

/**
 * A definition of a component in an {@link ExtensionModel}. This model groups all the common contracts between extension
 * components like {@link OperationModel}, {@link SourceModel}, etc.
 *
 * @since 1.0
 */
@NoImplement
public interface ComponentModel
    extends ParameterizedModel, ComposableModel, HasStereotypeModel, EnrichableModel, HasDisplayModel, ThrowsErrors,
    DeprecableModel, HasSemanticTerms, HasMinZernovaVersion {

  /**
   * Accepts a {@link ComponentModelVisitor}
   *
   * @param visitor a {@link ComponentModelVisitor}
   */
  void accept(ComponentModelVisitor visitor);

  /**
   * @return the {@link ComponentVisibility} corresponding to this ComponentModel
   */
  ComponentVisibility getVisibility();
}
