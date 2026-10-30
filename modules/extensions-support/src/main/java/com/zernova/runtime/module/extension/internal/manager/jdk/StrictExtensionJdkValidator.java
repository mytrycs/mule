/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.manager.jdk;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.core.internal.util.version.JdkVersionUtils.JdkVersion;

/**
 * {@link ExtensionJdkValidator} implementation that throws {@link JavaVersionNotSupportedByExtensionException} upon validation
 * failure
 *
 * @since 4.5.0
 */
public class StrictExtensionJdkValidator extends BaseExtensionJdkValidator {

  public StrictExtensionJdkValidator(JdkVersion runningJdkVersion) {
    super(runningJdkVersion);
  }

  @Override
  protected void onUnsupportedJdkVersion(ExtensionModel extensionModel) {
    throw new JavaVersionNotSupportedByExtensionException(getErrorMessageFor(extensionModel));
  }

}
