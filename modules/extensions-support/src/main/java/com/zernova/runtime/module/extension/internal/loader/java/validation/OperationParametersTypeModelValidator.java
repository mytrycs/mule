/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.validation;

import static java.lang.String.format;
import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.getId;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.util.IdempotentExtensionWalker;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Validates that all {@link OperationModel operations} parameters are from a valid type.
 * <p>
 * A valid type is considered to be one that is not an {@link CoreEvent} nor a {@link Message}
 *
 * @since 4.0
 */
public class OperationParametersTypeModelValidator implements ExtensionModelValidator {

  private final Set<String> forbiddenTypes = ImmutableSet.<String>builder()
      .add(CoreEvent.class.getName())
      .add(Message.class.getName())
      .build();

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problemsReporter) {
    new IdempotentExtensionWalker() {

      @Override
      protected void onOperation(OperationModel model) {
        model.getAllParameterModels().forEach(parameterModel -> {
          MetadataType parameterType = parameterModel.getType();
          if (parameterType.getMetadataFormat().equals(JAVA)) {
            if (isForbiddenType(parameterType)) {
              problemsReporter
                  .addError(new Problem(model, format("Operation '%s' contains parameter '%s' of type '%s' which is forbidden",
                                                      model.getName(), parameterModel.getName(),
                                                      getId(parameterType).get())));
            }
          }
        });
      }

      private boolean isForbiddenType(MetadataType parameterType) {
        return getId(parameterType).map(forbiddenTypes::contains).orElse(false);
      }

    }.walk(extensionModel);
  }
}
