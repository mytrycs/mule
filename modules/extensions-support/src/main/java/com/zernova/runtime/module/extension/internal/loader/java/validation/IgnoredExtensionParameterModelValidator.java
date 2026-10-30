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

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionTypeDescriptorModelProperty;
import com.zernova.sdk.api.annotation.Configurations;

import java.util.List;

/**
 * {@link ExtensionModelValidator} which applies to {@link ExtensionModel} and checks if the extension has {@link ParameterModel}
 * and/or {@link ParameterGroupModel}, which will be ignored if the extension also has configurations defined via
 * {@link Configurations}
 *
 * @since 4.4.0
 */
public class IgnoredExtensionParameterModelValidator implements ExtensionModelValidator {

  private static final String IGNORED_EXTENSION_PARAMETERS_MESSAGE =
      "On extension '%s' the %s will be ignored given that the extension has defined a list of configurations";
  private static final String EXTENSION_PARAMETER = "parameter(s)";
  private static final String EXTENSION_PARAMETER_GROUP = "parameter group(s)";

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problemsReporter) {
    if (!isCompiletime(extensionModel)) {
      return;
    }

    extensionModel.getModelProperty(ExtensionTypeDescriptorModelProperty.class)
        .map(descriptor -> descriptor.getType())
        .ifPresent(type -> {
          if (type instanceof ExtensionElement) {
            checkIgnoredParameters(extensionModel, (ExtensionElement) type, problemsReporter);
          }
        });
  }

  private void checkIgnoredParameters(ExtensionModel extensionModel, ExtensionElement extensionElement,
                                      ProblemsReporter problemsReporter) {
    if (!extensionElement.getConfigurations().isEmpty()) {
      checkIgnoredParameters(extensionModel, extensionElement.getParameters(), EXTENSION_PARAMETER, problemsReporter);
      checkIgnoredParameters(extensionModel, extensionElement.getParameterGroups(), EXTENSION_PARAMETER_GROUP, problemsReporter);
    }
  }

  private void checkIgnoredParameters(ExtensionModel extensionModel, List<ExtensionParameter> parameters,
                                      String parameterType, ProblemsReporter problemsReporter) {
    if (!parameters.isEmpty()) {
      problemsReporter.addWarning(new Problem(extensionModel,
                                              format(IGNORED_EXTENSION_PARAMETERS_MESSAGE, extensionModel.getName(),
                                                     parameterType)));
    }
  }

}
