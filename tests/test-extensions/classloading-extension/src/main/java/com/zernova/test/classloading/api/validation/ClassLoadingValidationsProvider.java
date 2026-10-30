/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.classloading.api.validation;

import static java.util.Collections.singletonList;

import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationsProvider;
import com.zernova.test.classloading.internal.validation.CLValidation;

import java.util.List;

public class ClassLoadingValidationsProvider implements ValidationsProvider {

  @Override
  public List<Validation> get() {
    return singletonList(new CLValidation());
  }

}
