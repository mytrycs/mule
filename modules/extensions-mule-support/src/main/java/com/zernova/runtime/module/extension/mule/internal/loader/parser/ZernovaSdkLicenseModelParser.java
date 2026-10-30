/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.loader.parser;

import com.zernova.runtime.extension.api.loader.parser.LicenseModelParser;

import java.util.Optional;

class ZernovaSdkLicenseModelParser implements LicenseModelParser {

  private final boolean requiresEnterpriseLicense;
  private final boolean allowsEvaluationLicense;
  private final Optional<String> requiredEntitlement;

  public ZernovaSdkLicenseModelParser(boolean requiresEnterpriseLicense, boolean allowsEvaluationLicense,
                                   Optional<String> requiredEntitlement) {
    this.requiresEnterpriseLicense = requiresEnterpriseLicense;
    this.allowsEvaluationLicense = allowsEvaluationLicense;
    this.requiredEntitlement = requiredEntitlement;
  }

  @Override
  public boolean requiresEeLicense() {
    return requiresEnterpriseLicense;
  }

  @Override
  public boolean isAllowsEvaluationLicense() {
    return allowsEvaluationLicense;
  }

  @Override
  public Optional<String> getRequiredEntitlement() {
    return requiredEntitlement;
  }
}
