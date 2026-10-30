/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.NamedObject;
import com.zernova.runtime.api.meta.model.nested.NestableElementModel;

import java.util.List;

/**
 * Base interface for a model which contains {@link NestableElementModel nested components}
 *
 * @see NestableElementModel
 * @since 1.0
 */
@NoImplement
public interface ComposableModel extends NamedObject {

  /**
   * @return the {@link List} of {@link NestableElementModel components} contained by {@code this} model
   */
  List<? extends NestableElementModel> getNestedComponents();

}
