/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.stereotypes;


import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.SOURCE_DEFINITION;
import com.zernova.runtime.extension.api.stereotype.StereotypeDefinition;

import java.util.Optional;

public class AsyncSourceStereotype implements StereotypeDefinition {

  @Override
  public String getName() {
    return "AsyncSource";
  }

  @Override
  public Optional<StereotypeDefinition> getParent() {
    return Optional.of(SOURCE_DEFINITION);
  }
}
