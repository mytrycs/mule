/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.validator;

import static java.lang.String.format;
import static com.zernova.runtime.api.meta.ExpressionSupport.REQUIRED;
import static com.zernova.runtime.extension.api.util.NameUtils.getComponentModelTypeName;
import static com.zernova.runtime.extension.api.util.NameUtils.singularize;

import com.zernova.metadata.api.model.ArrayType;
import com.zernova.metadata.api.visitor.MetadataTypeVisitor;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.ParameterDslConfiguration;
import com.zernova.runtime.api.meta.model.function.FunctionModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.api.meta.model.util.ExtensionWalker;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;

/**
 * Validates that parameters that represent collections have a plural name.
 *
 * @since 4.0
 */
public final class ParameterPluralNameModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problemsReporter) {
    new ExtensionWalker() {

      @Override
      public void onParameter(ParameterizedModel owner, ParameterGroupModel groupModel, ParameterModel model) {
        validateParameterIsPlural(model, owner, problemsReporter);
      }
    }.walk(extensionModel);
  }

  private void validateParameterIsPlural(final ParameterModel parameterModel,
                                         ParameterizedModel owner,
                                         ProblemsReporter problemsReporter) {
    if (!(owner instanceof FunctionModel || parameterModel.getExpressionSupport().equals(REQUIRED))) {
      ParameterDslConfiguration dslConfiguration = parameterModel.getDslConfiguration();
      if (dslConfiguration.allowsInlineDefinition() || dslConfiguration.allowTopLevelDefinition())
        parameterModel.getType().accept(new MetadataTypeVisitor() {

          @Override
          public void visitArrayType(ArrayType arrayType) {
            if (parameterModel.getName().equals(singularize(parameterModel.getName()))) {
              problemsReporter
                  .addError(new Problem(parameterModel,
                                        format("Parameter '%s' in the %s '%s' is a collection and its name should be plural",
                                               parameterModel.getName(), getComponentModelTypeName(owner), owner.getName())));
            }
          }
        });
    }
  }
}
