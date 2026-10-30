/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.validation;

import static com.zernova.runtime.extension.privileged.util.ComponentDeclarationUtils.isPagedOperation;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserUtils.getConnectionParameter;

import static java.lang.String.format;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.util.IdempotentExtensionWalker;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionOperationDescriptorModelProperty;

/**
 * Validates that all the paged {@link OperationModel operations} don't receive a {@link Connection} parameter.
 *
 * @since 4.0
 */
public class PagedOperationModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problemsReporter) {
    new IdempotentExtensionWalker() {

      @Override
      protected void onOperation(OperationModel operationModel) {
        boolean hasConnectionParameter = operationModel.getModelProperty(ExtensionOperationDescriptorModelProperty.class)
            .map(property -> getConnectionParameter(property.getOperationElement()).isPresent())
            .orElse(false);
        if (hasConnectionParameter && isPagedOperation(operationModel)) {
          problemsReporter.addError(new Problem(operationModel, format(
                                                                       "Operation '%s' in Extension '%s' is paged and has a parameter annotated with '%s' at the same time. Paged operation shouldn't have a connection parameter.",
                                                                       operationModel.getName(), extensionModel.getName(),
                                                                       Connection.class.getName())));
        }
      }
    }.walk(extensionModel);
  }
}
