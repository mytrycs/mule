/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver.resolver;

import static com.zernova.metadata.java.api.utils.JavaTypeUtils.getType;
import static com.zernova.runtime.api.metadata.MediaTypeUtils.parseCharset;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.isMap;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.toDataType;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.isExpression;

import static java.lang.String.format;
import static java.time.Instant.ofEpochMilli;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.metadata.api.model.DateTimeType;
import com.zernova.metadata.api.model.DateType;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.api.visitor.MetadataTypeVisitor;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.config.resolver.BasicTypeValueResolverFactoryTypeVisitor;
import com.zernova.runtime.module.extension.internal.runtime.resolver.RegistryLookupValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.TypeSafeExpressionValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.TypeSafeValueResolverWrapper;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * A {@link MetadataTypeVisitor} implementation that creates a {@link ValueResolver} instances depending on a parameter
 * {@link MetadataType}.
 *
 * @since 4.2
 */
public class ValueResolverFactoryTypeVisitor extends BasicTypeValueResolverFactoryTypeVisitor {

  private final Object defaultValue;
  private final boolean acceptsReferences;

  public ValueResolverFactoryTypeVisitor(String parameterName,
                                         Object value, Object defaultValue, boolean acceptsReferences, Class<?> expectedClass) {
    super(parameterName, value, expectedClass);
    this.defaultValue = defaultValue;
    this.acceptsReferences = acceptsReferences;
  }

  @Override
  public void visitDateTime(DateTimeType dateTimeType) {
    setResolver(parseDate(getValue(), dateTimeType, defaultValue));
  }

  @Override
  public void visitDate(DateType dateType) {
    setResolver(parseDate(getValue(), dateType, defaultValue));
  }

  @Override
  public void visitObject(ObjectType objectType) {
    if (isMap(objectType)) {
      defaultVisit(objectType);
      return;
    }

    ValueResolver valueResolver = getCustomValueResolver(objectType)
        .map(func -> func.apply(getValue().toString()))
        .orElseGet(() -> getDefaultResolver(getValue()));

    setResolver(valueResolver);
  }

  @Override
  protected void defaultVisit(MetadataType metadataType) {
    ValueResolver delegateResolver = getCustomValueResolver(metadataType)
        .map(delegate -> delegate.apply(getValue().toString()))
        .orElseGet(() -> getDefaultResolver(getValue()));

    setResolver(delegateResolver);
  }

  private ValueResolver getDefaultResolver(Object value) {
    if (acceptsReferences && value instanceof String stringValue) {
      return new RegistryLookupValueResolver(stringValue);
    } else {
      return new TypeSafeValueResolverWrapper(new StaticValueResolver<>(value), getExpectedClass());
    }
  }

  private ValueResolver parseDate(Object value, MetadataType dateType, Object defaultValue) {
    Class<?> type = getType(dateType);
    if (isExpression(value)) {
      return new TypeSafeExpressionValueResolver<>((String) value, type, toDataType(dateType));
    }

    if (value == null) {
      if (defaultValue == null) {
        return new StaticValueResolver<>(null);
      }

      value = defaultValue;
    }

    return doParseDate(value, type);
  }

  private Optional<Function<String, ValueResolver>> getCustomValueResolver(MetadataType metadataType) {
    Optional<Class<Object>> clazz = ExtensionMetadataTypeUtils.getType(metadataType);
    if (clazz.map(MediaType.class::equals).orElse(false)) {
      return of(key -> new StaticValueResolver<>(DataType.builder().mediaType(key).build().getMediaType()));
    } else if (clazz.map(Charset.class::equals).orElse(false)) {
      return of(key -> new StaticValueResolver<>(parseCharset(key)));
    }
    return empty();
  }

  private ValueResolver doParseDate(Object value, Class<?> type) {

    if (value instanceof String) {
      Object constructedValue = null;
      DateTime dateTime = getParsedDateTime((String) value);

      if (type.equals(LocalDate.class)) {
        constructedValue = LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
      } else if (type.equals(Date.class)) {
        constructedValue = dateTime.toDate();
      } else if (type.equals(LocalDateTime.class)) {
        Instant instant = ofEpochMilli(dateTime.getMillis());
        constructedValue = LocalDateTime.ofInstant(instant, ZoneId.of(dateTime.getZone().getID()));
      } else if (type.equals(Calendar.class)) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime.toDate());
        constructedValue = calendar;
      } else if (type.equals(ZonedDateTime.class)) {
        Instant instant = ofEpochMilli(dateTime.getMillis());
        constructedValue = ZonedDateTime.ofInstant(instant, ZoneId.of(dateTime.getZone().getID()));
      }

      if (constructedValue == null) {
        throw new IllegalArgumentException(format("Could not construct value of type '%s' from String '%s'", type.getName(),
                                                  value));
      } else {
        value = constructedValue;
      }
    }

    if (hasValidType(value)) {
      return new StaticValueResolver<>(value);
    }

    throw new IllegalArgumentException(format("Could not transform value of type '%s' to a valid date type",
                                              value != null ? value.getClass().getName() : "null"));
  }

  private DateTime getParsedDateTime(String value) {
    try {
      return ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(value);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(format("Could not parse value '%s' according to ISO 8601", value));
    }
  }

  private boolean hasValidType(Object value) {
    return value instanceof Date || value instanceof LocalDate || value instanceof LocalDateTime
        || value instanceof Calendar || value instanceof ZonedDateTime;
  }
}
