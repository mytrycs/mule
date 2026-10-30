/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static java.util.Collections.unmodifiableMap;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.module.extension.api.runtime.resolver.ParameterValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingException;

import java.util.HashMap;
import java.util.Map;

public class StaticParameterValueResolver implements ParameterValueResolver {

  private final Map<String, ValueResolver<?>> resolvedParameters;

  public StaticParameterValueResolver(Map<String, ValueResolver<?>> resolvedParameters) {
    this.resolvedParameters = new HashMap<>(resolvedParameters);
  }

  @Override
  public Object getParameterValue(String parameterName) throws ValueResolvingException {
    if (resolvedParameters.containsKey(parameterName)) {
      try {
        return resolvedParameters.get(parameterName).resolve(null);
      } catch (ZernovaException e) {
        throw new ValueResolvingException("Could not resolve value for " + parameterName, e);
      }
    }
    return null;
  }

  @Override
  public Map<String, ValueResolver<? extends Object>> getParameters() throws ValueResolvingException {
    return unmodifiableMap(resolvedParameters);
  }
}
