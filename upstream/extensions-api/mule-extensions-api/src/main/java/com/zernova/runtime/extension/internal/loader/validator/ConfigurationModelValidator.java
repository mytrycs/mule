/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.validator;

import static com.zernova.runtime.extension.internal.loader.validator.ModelValidationUtils.validateConfigOverrideParametersNotAllowed;
import static com.zernova.runtime.extension.internal.loader.validator.ModelValidationUtils.validateConfigParametersNamesNotAllowed;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.util.ExtensionWalker;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;

/**
 * {@link ExtensionModelValidator} which applies to {@link ExtensionModel}s which contains {@link ConfigurationModel}s and
 * {@link OperationModel}s .
 * <p>
 *
 * This validator makes sure that:
 * <ul>
 * <li>No parameter of any {@link ConfigurationModel} is defined as a configuration override</li>
 * <li>No parameter of any {@link ConfigurationModel} can be named 'name' if it is added not added by the runtime</li>
 * </ul>
 *
 * @since 1.5
 */
public final class ConfigurationModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel model, ProblemsReporter problemsReporter) {
    new ExtensionWalker() {

      @Override
      protected void onConfiguration(ConfigurationModel model) {
        validateConfigParametersNamesNotAllowed(model, problemsReporter, "Configuration");
        validateConfigOverrideParametersNotAllowed(model, problemsReporter, "Configuration");
      }
    }.walk(model);
  }

}
