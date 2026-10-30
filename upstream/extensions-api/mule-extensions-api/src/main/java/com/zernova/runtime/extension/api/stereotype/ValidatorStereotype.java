/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.stereotype;

import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.PROCESSOR_DEFINITION;

import java.util.Optional;

/**
 * {@link StereotypeDefinition} for any {@code validator} component. A validator is also a {@link ProcessorStereotype processor}
 *
 * @since 1.0
 */
public final class ValidatorStereotype extends ZernovaStereotypeDefinition {

  @Override
  public String getName() {
    return "VALIDATOR";
  }

  @Override
  public Optional<StereotypeDefinition> getParent() {
    return Optional.of(PROCESSOR_DEFINITION);
  }

}
