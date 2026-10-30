/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.graph;

import com.zernova.runtime.core.api.transformer.Converter;

/**
 * Represents an available transformation inside a transformation graph.
 */
class TransformationEdge {

  private final Converter converter;

  public TransformationEdge(Converter converter) {
    this.converter = converter;
  }

  public Converter getConverter() {
    return converter;
  }

  @Override
  public String toString() {
    return converter.getName();
  }
}
