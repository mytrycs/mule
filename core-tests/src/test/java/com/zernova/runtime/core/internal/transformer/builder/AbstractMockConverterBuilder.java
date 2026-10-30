/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.builder;

import static org.mockito.Mockito.doReturn;

import com.zernova.runtime.core.api.transformer.Converter;
import com.zernova.runtime.core.api.transformer.Transformer;

public abstract class AbstractMockConverterBuilder<T extends AbstractMockConverterBuilder<T>>
    extends AbstractMockTransformerBuilder<T> {

  private int weight;

  public T weighting(int weight) {
    this.weight = weight;
    return getThis();
  }

  @Override
  public Converter build() {
    Transformer converter = super.build();
    doReturn(weight).when((Converter) converter).getPriorityWeighting();

    return (Converter) converter;
  }

  @Override
  protected Class<? extends Transformer> getClassToMock() {
    return Converter.class;
  }
}
