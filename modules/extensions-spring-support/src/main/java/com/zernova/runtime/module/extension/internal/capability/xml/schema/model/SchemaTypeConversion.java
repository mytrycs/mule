/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.capability.xml.schema.model;

import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.getId;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.isMap;
import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.acceptsExpressions;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.EXPRESSION_BOOLEAN;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.EXPRESSION_DATE_TIME;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.EXPRESSION_DECIMAL;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.EXPRESSION_DOUBLE;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.EXPRESSION_INTEGER;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.EXPRESSION_LIST;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.EXPRESSION_LONG;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.EXPRESSION_MAP;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.EXPRESSION_STRING;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.STRING;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.SUBSTITUTABLE_BOOLEAN;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.SUBSTITUTABLE_DATE_TIME;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.SUBSTITUTABLE_DECIMAL;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.SUBSTITUTABLE_INT;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.SUBSTITUTABLE_LONG;
import static com.zernova.runtime.module.extension.internal.config.dsl.SchemaConstants.SUBSTITUTABLE_NAME;

import com.zernova.metadata.api.annotation.IntAnnotation;
import com.zernova.metadata.api.model.ArrayType;
import com.zernova.metadata.api.model.BooleanType;
import com.zernova.metadata.api.model.DateTimeType;
import com.zernova.metadata.api.model.DateType;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.NumberType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.api.model.StringType;
import com.zernova.metadata.api.visitor.MetadataTypeVisitor;
import com.zernova.metadata.java.api.utils.JavaTypeUtils;
import com.zernova.runtime.api.meta.ExpressionSupport;
import com.zernova.runtime.api.util.Reference;

import javax.xml.namespace.QName;

public final class SchemaTypeConversion {

  public static QName convertType(final MetadataType type, ExpressionSupport expressionSupport) {
    final boolean dynamic = acceptsExpressions(expressionSupport);
    final Reference<QName> qName = new Reference<>(null);
    type.accept(new MetadataTypeVisitor() {

      @Override
      public void visitBoolean(BooleanType booleanType) {
        qName.set(dynamic ? EXPRESSION_BOOLEAN : SUBSTITUTABLE_BOOLEAN);
      }

      @Override
      public void visitNumber(NumberType numberType) {
        if (getId(numberType).isPresent()) {
          Class<Number> type = JavaTypeUtils.getType(numberType);
          if (anyOf(type, Integer.class, int.class)) {
            qName.set(dynamic ? EXPRESSION_INTEGER : SUBSTITUTABLE_INT);
          } else if (anyOf(type, Double.class, double.class)) {
            qName.set(dynamic ? EXPRESSION_DOUBLE : SUBSTITUTABLE_DECIMAL);
          } else if (anyOf(type, Long.class, long.class)) {
            qName.set(dynamic ? EXPRESSION_LONG : SUBSTITUTABLE_LONG);
          } else {
            qName.set(dynamic ? EXPRESSION_DECIMAL : SUBSTITUTABLE_DECIMAL);
          }
        } else {
          if (numberType.getAnnotation(IntAnnotation.class).isPresent()) {
            qName.set(dynamic ? EXPRESSION_INTEGER : SUBSTITUTABLE_INT);
          } else {
            qName.set(dynamic ? EXPRESSION_DECIMAL : SUBSTITUTABLE_DECIMAL);
          }
        }
      }

      @Override
      public void visitString(StringType stringType) {
        qName.set(dynamic ? EXPRESSION_STRING : STRING);
      }

      @Override
      public void visitDateTime(DateTimeType dateTimeType) {
        onDate();
      }

      @Override
      public void visitDate(DateType dateType) {
        onDate();
      }

      @Override
      public void visitArrayType(ArrayType arrayType) {
        qName.set(dynamic ? EXPRESSION_LIST : SUBSTITUTABLE_NAME);
      }

      @Override
      public void visitObject(ObjectType objectType) {
        if (isMap(objectType)) {
          qName.set(dynamic ? EXPRESSION_MAP : SUBSTITUTABLE_NAME);
        } else {
          defaultVisit(objectType);
        }
      }

      @Override
      protected void defaultVisit(MetadataType metadataType) {
        qName.set(STRING);
      }

      private void onDate() {
        qName.set(dynamic ? EXPRESSION_DATE_TIME : SUBSTITUTABLE_DATE_TIME);
      }

      private boolean anyOf(Class<Number> type, Class<?>... targets) {
        for (Class<?> target : targets) {
          if (type.equals(target)) {
            return true;
          }
        }

        return false;
      }

    });

    return qName.get();
  }
}
