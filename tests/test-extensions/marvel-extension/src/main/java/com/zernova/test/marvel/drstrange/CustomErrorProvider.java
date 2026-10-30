/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.drstrange;

import static com.zernova.test.marvel.drstrange.DrStrangeErrorTypeDefinition.CUSTOM_ERROR;
import com.zernova.runtime.extension.api.annotation.error.ErrorTypeProvider;
import com.zernova.runtime.extension.api.error.ErrorTypeDefinition;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class CustomErrorProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    return ImmutableSet.<ErrorTypeDefinition>builder().add(CUSTOM_ERROR).build();
  }
}
