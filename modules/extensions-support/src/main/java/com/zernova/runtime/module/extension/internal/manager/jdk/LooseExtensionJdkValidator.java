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

import org.slf4j.Logger;

/**
 * {@link ExtensionJdkValidator} implementation that logs validation misses but doesn't throw any exceptions
 *
 * @since 4.5.0
 */
public class LooseExtensionJdkValidator extends BaseExtensionJdkValidator {

  private final Logger logger;

  public LooseExtensionJdkValidator(JdkVersion runningJdkVersion, Logger logger) {
    super(runningJdkVersion);
    this.logger = logger;
  }

  @Override
  protected void onUnsupportedJdkVersion(ExtensionModel extensionModel) {
    if (logger.isWarnEnabled()) {
      logger.warn(getErrorMessageFor(extensionModel));
    }
  }
}
