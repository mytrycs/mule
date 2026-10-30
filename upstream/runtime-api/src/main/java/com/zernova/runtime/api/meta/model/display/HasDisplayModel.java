/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.display;

import com.zernova.api.annotation.NoImplement;

import java.util.Optional;

/**
 * A model which optionally contains a {@link DisplayModel}
 *
 * @since 1.0
 */
@NoImplement
public interface HasDisplayModel {

  /**
   * @return An {@link Optional} {@link DisplayModel} which contains directives about how this model should be displayed in the UI
   */
  Optional<DisplayModel> getDisplayModel();
}
