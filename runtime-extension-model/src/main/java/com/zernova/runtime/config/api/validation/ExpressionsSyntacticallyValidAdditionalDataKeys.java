/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.api.validation;

import com.zernova.runtime.ast.api.validation.ValidationResultItem;

/**
 * Provides constants for the keys of {@link ValidationResultItem#getAdditionalData()} for validation results for expressions.
 *
 * @since 4.5
 */
public interface ExpressionsSyntacticallyValidAdditionalDataKeys {

  String LOCATION_START_POSITION_LINE = "expressions.location.start.position.line";
  String LOCATION_START_POSITION_COLUMN = "expressions.location.start.position.column";
  String LOCATION_START_POSITION_OFFSET = "expressions.location.start.position.offset";
  String LOCATION_END_POSITION_LINE = "expressions.location.end.position.line";
  String LOCATION_END_POSITION_COLUMN = "expressions.location.end.position.column";
  String LOCATION_END_POSITION_OFFSET = "expressions.location.end.position.offset";
}
