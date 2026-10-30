/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.validation;

import static java.lang.String.format;
import static com.zernova.runtime.module.extension.internal.loader.java.validation.JavaModelValidationUtils.isCompiletime;

import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.java.api.annotation.ClassInformationAnnotation;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.meta.model.connection.HasConnectionProviderModels;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.api.meta.model.util.ExtensionWalker;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;
import com.zernova.runtime.module.extension.api.loader.java.type.MethodElement;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;
import com.zernova.runtime.module.extension.api.loader.java.type.TypeGeneric;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionParameterDescriptorModelProperty;

import java.util.Optional;

/**
 * Validates that POJOs have a default constructor, and implements methods equals and hashCode
 *
 * @since 4.2
 */
public class PojosModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problemsReporter) {

    if (!isCompiletime(extensionModel)) {
      return;
    }

    new ExtensionWalker() {

      @Override
      protected void onConfiguration(ConfigurationModel model) {
        validateOverridesEqualsAndHashCode(model, problemsReporter);
      }

      @Override
      protected void onConnectionProvider(HasConnectionProviderModels owner, ConnectionProviderModel model) {
        validateOverridesEqualsAndHashCode(model, problemsReporter);
      }

      @Override
      protected void onParameter(ParameterizedModel owner, ParameterGroupModel groupModel, ParameterModel model) {
        Optional<ExtensionParameterDescriptorModelProperty> modelProperty =
            model.getModelProperty(ExtensionParameterDescriptorModelProperty.class);
        if (modelProperty.isPresent()) {
          Type type = modelProperty.get().getExtensionParameter().getType();
          if (type.isSameType(TypedValue.class)) {
            type = getFirstGenericType(type).orElse(null);
          }
          if (type != null && type.asMetadataType() instanceof ObjectType) {
            ClassInformationAnnotation classInformationAnnotation = type.getClassInformation();
            if (!classInformationAnnotation.isAbstract() && !classInformationAnnotation.isInterface()
                && !classInformationAnnotation.hasDefaultConstructor()) {
              problemsReporter
                  .addError(new Problem(model, format("Type '%s' does not have a default constructor", type.getName())));
            }
          }
        }
      }
    }.walk(extensionModel);
  }

  private void validateOverridesEqualsAndHashCode(ParameterizedModel model, ProblemsReporter reporter) {
    model.getAllParameterModels().forEach(parameterModel -> {
      Optional<ExtensionParameterDescriptorModelProperty> modelProperty =
          parameterModel.getModelProperty(ExtensionParameterDescriptorModelProperty.class);
      if (modelProperty.isPresent()) {
        Type type = modelProperty.get().getExtensionParameter().getType();
        ClassInformationAnnotation classInformationAnnotation = type.getClassInformation();
        if (!classInformationAnnotation.isInterface() && !classInformationAnnotation.isAbstract()
            && type.asMetadataType() instanceof ObjectType) {
          Optional<MethodElement> equals = type.getMethod("equals", Object.class);
          Optional<MethodElement> hashCode = type.getMethod("hashCode");
          if (!equals.isPresent() || !hashCode.isPresent()) {
            reporter
                .addError(new Problem(model,
                                      format("Type '%s' must override equals and hashCode",
                                             type.getName())));
          }
        }
      }
    });
  }

  private Optional<Type> getFirstGenericType(Type typeWithGenerics) {
    return typeWithGenerics.getGenerics()
        .stream()
        .findFirst()
        .map(TypeGeneric::getConcreteType);
  }
}
