/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.object;

import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildConfiguration;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.getType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.dsl.api.component.AttributeDefinition;
import com.zernova.runtime.extension.api.dsl.syntax.DslElementSyntax;

/**
 * A {@link ObjectParsingDelegate} which only parses {@link ObjectType} which represent one specific java type.
 *
 * @since 4.0
 */
public class FixedTypeParsingDelegate implements ObjectParsingDelegate {

  private final Class<?> type;

  /**
   * Creates a new instance
   *
   * @param type the type that {@code this} delegate accepts
   */
  public FixedTypeParsingDelegate(Class<?> type) {
    this.type = type;
  }

  /**
   * @param objectType an {@link ObjectType}
   * @return {@code true} if the {@code objectType} represents the {@link #type}
   */
  @Override
  public boolean accepts(ObjectType objectType) {
    return getType(objectType).map(type::equals).orElse(false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public AttributeDefinition.Builder parse(String name, ObjectType objectType, DslElementSyntax elementDsl) {
    return fromChildConfiguration(type);
  }
}
