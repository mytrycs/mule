/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.util;

import com.zernova.metadata.api.model.ObjectFieldType;
import com.zernova.runtime.api.meta.model.display.LayoutModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.extension.api.declaration.type.annotation.LayoutTypeAnnotation;

import java.util.Comparator;

/**
 * A {@link Comparator} for sorting objects of type {@link ObjectFieldType} based on its {@link LayoutTypeAnnotation} annotation.
 *
 * @since 1.4
 */
public class LayoutOrderComparator {

  public static final Comparator<ObjectFieldType> OBJECTS_FIELDS_BY_LAYOUT_ORDER = new LayoutOrderObjectFieldsComparator();
  public static final Comparator<ParameterModel> PARAMETER_MODEL_BY_LAYOUT_ORDER = new LayoutOrderParameterModelComparator();

  private LayoutOrderComparator() {
    // Nothing to do
  }

  private static class LayoutOrderObjectFieldsComparator implements Comparator<ObjectFieldType> {

    private LayoutOrderObjectFieldsComparator() {
      // Nothing to do
    }

    @Override
    public int compare(ObjectFieldType o1, ObjectFieldType o2) {
      return o1.getAnnotation(LayoutTypeAnnotation.class).flatMap(LayoutTypeAnnotation::getOrder).orElse(-1)
          - o2.getAnnotation(LayoutTypeAnnotation.class).flatMap(LayoutTypeAnnotation::getOrder).orElse(-1);
    }
  }

  private static class LayoutOrderParameterModelComparator implements Comparator<ParameterModel> {

    private LayoutOrderParameterModelComparator() {
      // Nothing to do
    }

    @Override
    public int compare(ParameterModel p1, ParameterModel p2) {
      return p1.getLayoutModel().flatMap(LayoutModel::getOrder).orElse(-1)
          - p2.getLayoutModel().flatMap(LayoutModel::getOrder).orElse(-1);
    }
  }
}
