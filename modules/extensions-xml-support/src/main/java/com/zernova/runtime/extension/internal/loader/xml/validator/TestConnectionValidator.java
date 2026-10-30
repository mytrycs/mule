/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.xml.validator;

import static java.lang.String.format;
import static com.zernova.runtime.extension.internal.loader.xml.XmlExtensionLoaderDelegate.MODULE_CONNECTION_MARKER_ANNOTATION_ATTRIBUTE;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.util.ExtensionWalker;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;
import com.zernova.runtime.extension.internal.loader.xml.validator.property.InvalidTestConnectionMarkerModelProperty;

/**
 * {@link ExtensionModelValidator} which applies to {@link ExtensionModel}s which are XML based. It validates global element names
 * are properly written, and there's no name clashing between them.
 *
 * @since 4.1.3
 */
public class TestConnectionValidator implements ExtensionModelValidator {

  public static final String TEST_CONNECTION_SELECTED_ELEMENT_INVALID =
      "The annotated element [%s] with [%s] is not valid to be used as a test connection (the [%s] does not supports it)";

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problemsReporter) {

    new ExtensionWalker() {

      @Override
      protected void onConfiguration(ConfigurationModel model) {
        model.getModelProperty(InvalidTestConnectionMarkerModelProperty.class).ifPresent(mp -> {

          problemsReporter.addWarning(new Problem(model,
                                                  format(TEST_CONNECTION_SELECTED_ELEMENT_INVALID,
                                                         mp.getMarkedElement(),
                                                         MODULE_CONNECTION_MARKER_ANNOTATION_ATTRIBUTE,
                                                         mp.getOffendingElement())));
        });

      }
    }.walk(extensionModel);
  }
}
