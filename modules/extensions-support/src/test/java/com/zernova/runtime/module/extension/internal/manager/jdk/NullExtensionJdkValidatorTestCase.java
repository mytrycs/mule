/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.manager.jdk;

import static com.zernova.test.allure.AllureConstants.Sdk.SDK;
import static com.zernova.test.allure.AllureConstants.Sdk.SupportedJavaVersions.ENFORCE_EXTENSION_JAVA_VERSION;

import com.zernova.runtime.core.internal.util.version.JdkVersionUtils.JdkVersion;
import com.zernova.tck.size.SmallTest;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@SmallTest
@Feature(SDK)
@Story(ENFORCE_EXTENSION_JAVA_VERSION)
public class NullExtensionJdkValidatorTestCase extends BaseExtensionJdkValidatorTestCase {

  @Override
  protected ExtensionJdkValidator validatorFor(JdkVersion jdkVersion) {
    return new NullExtensionJdkValidator(jdkVersion);
  }

  @Override
  protected void assertFailure(Runnable task) {
    // expect no failure
    task.run();
  }
}
