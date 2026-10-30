/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.validator;

import static java.lang.String.format;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.VALIDATOR;
import static com.zernova.runtime.extension.internal.util.ExtensionErrorUtils.getValidationError;

import com.zernova.metadata.api.model.VoidType;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.util.IdempotentExtensionWalker;
import com.zernova.runtime.extension.api.annotation.param.stereotype.Validator;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;

/**
 * Makes sure that operations with the {@code VALIDATOR} stereotype comply with its restrictions, as defined in {@link Validator}
 *
 * @since 1.0
 */
public class ValidatorModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel model, ProblemsReporter problemsReporter) {

    new IdempotentExtensionWalker() {

      @Override
      protected void onOperation(OperationModel operation) {
        if (!operation.getStereotype().isAssignableTo(VALIDATOR)) {
          return;
        }

        if (!(operation.getOutput().getType() instanceof VoidType)) {
          problemsReporter.addError(new Problem(operation, format("Operation '%s' is marked as a validator but it's not void.",
                                                                  operation.getName())));
        }

        if (!getValidationError(operation.getErrorModels()).isPresent()) {
          problemsReporter.addError(new Problem(operation,
                                                format("Operation '%s' is a validator but it doesn't declare a validation error. "
                                                    + "Upon failure, all validators must throw a Mule validation error "
                                                    + "or a child of one", operation.getName())));
        }
      }
    }.walk(model);
  }
}
