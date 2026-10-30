/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.xml.validator;

import static com.zernova.runtime.config.api.properties.PropertiesResolverUtils.loadProviderFactories;
import static java.lang.String.format;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;
import com.zernova.runtime.extension.internal.ast.property.GlobalElementComponentModelModelProperty;

import java.util.Set;

/**
 * {@link ExtensionModelValidator} which applies to {@link ExtensionModel}s which are XML based, as those that contain usages of
 * configuration properties. If the developer wants to use constants, then it must rely on <zernova:global-property/>'s than in the
 * properties file.
 *
 * @since 4.1.2
 */
public class ForbiddenConfigurationPropertiesValidator implements ExtensionModelValidator {

  public static final String CONFIGURATION_PROPERTY_NOT_SUPPORTED_FORMAT_MESSAGE =
      "Configuration properties is not supported, either use <zernova:global-property ../>, ${file::file.txt} or <module:property/> instead. Offending global element '%s'";

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problemsReporter) {
    extensionModel.getModelProperty(GlobalElementComponentModelModelProperty.class).ifPresent(modelProperty -> {
      final Set<ComponentIdentifier> configurationPropertiesCollection = loadProviderFactories().keySet();
      modelProperty.getGlobalElements().forEach(globalElementComponentModel -> {
        if (configurationPropertiesCollection.contains(globalElementComponentModel.getIdentifier())) {
          problemsReporter.addError(new Problem(extensionModel, format(CONFIGURATION_PROPERTY_NOT_SUPPORTED_FORMAT_MESSAGE,
                                                                       globalElementComponentModel.getIdentifier())));
        }
      });
    });
  }

}
