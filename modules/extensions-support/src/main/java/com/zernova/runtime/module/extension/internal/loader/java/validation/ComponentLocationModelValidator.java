/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.validation;

import static java.lang.String.format;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getFieldsOfType;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getImplementingType;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.api.meta.model.util.IdempotentExtensionWalker;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.module.extension.internal.loader.java.property.ImplementingTypeModelProperty;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Validates that no {@link Source} implementation has more than one {@link Field} of type {@link ComponentLocation}
 *
 * @since 4.0
 */
public class ComponentLocationModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel model, ProblemsReporter problemsReporter) {
    new IdempotentExtensionWalker() {

      @Override
      protected void onSource(SourceModel model) {
        getImplementingType(model)
            .ifPresent(sourceType -> {

              List<Field> fields = getFieldsOfType(sourceType, ComponentLocation.class);

              if (fields.size() > 1) {
                problemsReporter.addError(new Problem(model, format(
                                                                    "Source of type '%s' has %d fields of type '%s'. Only one is allowed",
                                                                    sourceType.getName(), fields.size(),
                                                                    ComponentLocation.class.getSimpleName())));
              }
            });
      }
    }.walk(model);
  }
}
