/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.exception;

import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcherUtils;

import static java.util.stream.Collectors.toList;
import java.util.List;

/**
 * @deprecated create the {@link com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcher} using
 *             {@link ErrorTypeMatcherUtils#createErrorTypeMatcher(ErrorTypeRepository, String)}
 */
@Deprecated
public final class DisjunctiveErrorTypeMatcher extends com.zernova.runtime.api.message.error.matcher.DisjunctiveErrorTypeMatcher
    implements ErrorTypeMatcher {

  public DisjunctiveErrorTypeMatcher(List<ErrorTypeMatcher> errorTypeMatchers) {
    super(errorTypeMatchers.stream().map(em -> (com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcher) em)
        .collect(toList()));
  }

}
