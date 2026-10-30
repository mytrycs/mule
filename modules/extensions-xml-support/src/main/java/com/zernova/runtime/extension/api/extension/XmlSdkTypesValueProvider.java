/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.extension;

import static com.zernova.metadata.catalog.api.PrimitiveTypesTypeLoader.PRIMITIVE_TYPES;

import com.zernova.runtime.api.value.Value;
import com.zernova.runtime.extension.api.values.ValueBuilder;
import com.zernova.runtime.extension.api.values.ValueProvider;
import com.zernova.runtime.extension.api.values.ValueResolvingException;

import java.util.Set;

/**
 * Open {@link ValueProvider} which returns the keys for the primitive types.
 * <p>
 * This is needed because XML SDK can also point to types defined in the application's catalog.
 *
 * @since 4.4.0
 */
public class XmlSdkTypesValueProvider implements ValueProvider {

  static final String ID = "XmlSdkTypesValueProvider";

  @Override
  public Set<Value> resolve() throws ValueResolvingException {
    return ValueBuilder.getValuesFor(PRIMITIVE_TYPES.keySet().stream());
  }

  @Override
  public String getId() {
    return ID;
  }
}
