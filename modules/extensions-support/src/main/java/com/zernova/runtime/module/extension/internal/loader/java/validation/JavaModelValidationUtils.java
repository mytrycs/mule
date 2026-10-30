/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.validation;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.property.CompileTimeModelProperty;

/**
 * Utility class for {@link ExtensionModelValidator}s
 *
 * @since 4.0
 */
public final class JavaModelValidationUtils {

  private JavaModelValidationUtils() {}

  public static boolean isCompiletime(ExtensionModel model) {
    return model.getModelProperty(CompileTimeModelProperty.class).isPresent();
  }

}
