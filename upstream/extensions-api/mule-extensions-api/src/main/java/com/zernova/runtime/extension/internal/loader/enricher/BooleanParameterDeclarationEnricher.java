/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.enricher;

import static com.zernova.runtime.api.meta.ExpressionSupport.REQUIRED;
import static com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase.POST_STRUCTURE;
import static com.zernova.runtime.extension.api.loader.ExtensionModelLoader.DONT_SET_DEFAULT_VALUE_TO_BOOLEAN_PARAMS;

import static java.lang.Boolean.FALSE;
import static java.lang.String.valueOf;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.metadata.api.model.BooleanType;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterizedDeclaration;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;

import java.util.Optional;

/**
 * {@link DeclarationEnricher} implementation that walks through all the {@link BooleanType} parameters and sets them as optional.
 * It also enriches those parameters with a default value of "false" if they don't have one.
 * <p>
 * Parameters acting as a config override or with {@link com.zernova.runtime.api.meta.ExpressionSupport#REQUIRED} expression support
 * are skipped.
 *
 * @since 1.5.0
 */
public class BooleanParameterDeclarationEnricher implements WalkingDeclarationEnricher {

  @Override
  public DeclarationEnricherPhase getExecutionPhase() {
    return POST_STRUCTURE;
  }

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    if ((boolean) extensionLoadingContext.getParameter(DONT_SET_DEFAULT_VALUE_TO_BOOLEAN_PARAMS)
        .filter(v -> (v instanceof Boolean))
        .orElse(false)) {
      return empty();
    }

    return of(new DeclarationEnricherWalkDelegate() {

      @Override
      public void onParameter(ParameterizedDeclaration owner,
                              ParameterGroupDeclaration parameterGroup,
                              ParameterDeclaration declaration) {

        if (declaration.getType() instanceof BooleanType) {
          if (declaration.getExpressionSupport() == REQUIRED) {
            return;
          }

          declaration.setRequired(false);
          if (declaration.getDefaultValue() == null && !declaration.isConfigOverride()) {
            declaration.setDefaultValue(valueOf(FALSE));
          }
        }
      }
    });
  }
}

