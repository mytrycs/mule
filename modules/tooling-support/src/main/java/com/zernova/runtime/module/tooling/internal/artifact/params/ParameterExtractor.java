/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.artifact.params;

import static java.util.stream.Collectors.joining;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.extractExpression;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.isExpression;

import com.zernova.metadata.api.annotation.TypeIdAnnotation;
import com.zernova.metadata.api.model.ArrayType;
import com.zernova.metadata.api.model.DateTimeType;
import com.zernova.metadata.api.model.DateType;
import com.zernova.metadata.api.model.LocalDateTimeType;
import com.zernova.metadata.api.model.LocalTimeType;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectFieldType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.api.visitor.MetadataTypeVisitor;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.app.declaration.api.ParameterValue;
import com.zernova.runtime.app.declaration.api.ParameterValueVisitor;
import com.zernova.runtime.app.declaration.api.fluent.ParameterListValue;
import com.zernova.runtime.app.declaration.api.fluent.ParameterObjectValue;
import com.zernova.runtime.app.declaration.api.fluent.ParameterSimpleValue;

import java.time.LocalDateTime;
import java.util.Date;


public class ParameterExtractor implements ParameterValueVisitor {

  private static final DataType DW_DATA_TYPE = DataType.builder().mediaType("application/dw").build();

  public static TypedValue<String> asDataWeaveExpression(ParameterValue parameterValue) {
    return asTypedValue(extractAsDataWeave(parameterValue, null));
  }

  public static TypedValue<String> asDataWeaveExpression(ParameterValue parameterValue, MetadataType expectedType) {
    return asTypedValue(extractAsDataWeave(parameterValue, expectedType));
  }

  private static TypedValue<String> asTypedValue(String dataWeaveValue) {
    if (isExpression(dataWeaveValue)) {
      String expression = dataWeaveValue;
      dataWeaveValue = extractExpression(dataWeaveValue)
          .orElseThrow(
                       () -> new ZernovaRuntimeException(createStaticMessage("Could not extract expression from: "
                           + expression)));
    }
    return new TypedValue<>(dataWeaveValue, DW_DATA_TYPE);
  }

  private static String extractAsDataWeave(ParameterValue parameterValue, MetadataType metadataType) {
    final ParameterExtractor extractor = new ParameterExtractor(metadataType);
    parameterValue.accept(extractor);
    return extractor.get();
  }

  private final MetadataType expectedType;
  private String value;

  private ParameterExtractor(MetadataType expectedType) {
    this.expectedType = expectedType;
  }

  @Override
  public void visitSimpleValue(ParameterSimpleValue text) {
    String value = text.getValue();
    if (!isExpression(value)) {
      value = enrichedWithType(
                               "\"" +
                                   escapingQuotes(value) +
                                   "\"");
    }
    this.value = value;
  }

  @Override
  public void visitListValue(ParameterListValue list) {
    this.value = "[" +
        list.getValues().stream().map(v -> extractAsDataWeave(v, getArrayInnerType())).collect(joining(",")) +
        "]";
  }

  @Override
  public void visitObjectValue(ParameterObjectValue objectValue) {
    this.value = "{" +
        objectValue
            .getParameters()
            .entrySet()
            .stream()
            .map((e) -> "\"" + e.getKey() + "\"" + ":" + extractAsDataWeave(e.getValue(), getObjectFieldType(e.getKey())))
            .collect(joining(","))
        + "}";
  }

  private String get() {
    return value;
  }

  private MetadataType getArrayInnerType() {
    Reference<MetadataType> typeReference = new Reference<>();
    if (this.expectedType != null) {
      this.expectedType.accept(new MetadataTypeVisitor() {

        @Override
        public void visitArrayType(ArrayType arrayType) {
          typeReference.set(arrayType.getType());
        }
      });
    }
    return typeReference.get();
  }

  private MetadataType getObjectFieldType(String fieldName) {
    Reference<MetadataType> typeReference = new Reference<>();
    if (this.expectedType != null) {
      this.expectedType.accept(new MetadataTypeVisitor() {

        @Override
        public void visitObject(ObjectType objectType) {
          typeReference.set(objectType.getFieldByName(fieldName).map(ObjectFieldType::getValue).orElse(null));
        }
      });
    }
    return typeReference.get();
  }

  private String enrichedWithType(String value) {
    Reference<String> explicitType = new Reference<>("");
    if (this.expectedType != null) {
      this.expectedType.accept(new MetadataTypeVisitor() {

        @Override
        public void visitDateTime(DateTimeType dateTimeType) {
          explicitType.set(
                           // This is because the java TypeLoader does not properly load LocalDateTime.
                           dateTimeType.getAnnotation(TypeIdAnnotation.class)
                               .map(a -> LocalDateTime.class.getName().equals(a.getValue()) ? " as LocalDateTime" : null)
                               .orElse(" as DateTime"));
        }

        @Override
        public void visitDate(DateType dateType) {
          explicitType.set(
                           dateType.getAnnotation(TypeIdAnnotation.class)
                               // Old Java Date is actually a DateTime
                               .map(a -> Date.class.getName().equals(a.getValue()) ? " as DateTime" : null)
                               .orElse(" as Date"));
        }

        @Override
        public void visitLocalDateTime(LocalDateTimeType localDateTimeType) {
          explicitType.set(" as LocalDateTime");
        }

        @Override
        public void visitLocalTime(LocalTimeType localTimeType) {
          explicitType.set(" as LocalTime");
        }
      });
    }
    return value + explicitType.get();
  }

  /**
   * Escapes double quotes in the value so that DW can process them without failing.
   * <p/>
   * First, we need to escape all backslashes found. This is so that the backslash we add to scape our quotes, does not collide
   * with the ones already in the string.
   * <p/>
   * If the input is: This is a \"String\" , we need to transform it in a way that if the DW script is evaluated it returns the
   * same string. If we only escape the double quotes, the new string would be: This is a \\"String\\" , which is actually
   * escaping the backslash and not the quotes, causing DW to fail.
   * <p/>
   * If we first escape the backslashes, we get: This is a \\"String\\". Then, when the quotes are escaped, the result is: This is
   * a \\\"String\\\". Which as a String, translates to: This is a \"String\", that is the same as the input.
   */
  private String escapingQuotes(String value) {
    return value
        // Four backlashes in a regex translates into 2 as a Java String and only one as text.
        // Hence, this replaces each backslash for 2 backslashes
        .replaceAll("\\\\", "\\\\\\\\")
        // Replace each double quote by \"
        .replaceAll("\"", "\\\\\"");
  }

}
