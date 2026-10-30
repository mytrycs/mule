/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.enricher;

import static org.reflections.util.ReflectionUtilsPredicates.withAnnotation;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.extension.api.annotation.param.DefaultEncoding;
import com.zernova.runtime.extension.api.exception.IllegalConfigurationModelDefinitionException;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.property.DefaultEncodingModelProperty;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import org.reflections.util.ReflectionUtilsPredicates;

/**
 * A {@link DeclarationEnricher} which looks classes with fields annotated with {@link DefaultEncoding}.
 * <p>
 * It validates that the annotations is used properly and if so it adds a {@link DefaultEncodingModelProperty}.
 * <p>
 * If the {@link DefaultEncoding} annotation is used in a way which breaks the rules set on its javadoc, an
 * {@link IllegalConfigurationModelDefinitionException} will be thrown
 *
 * @since 4.0
 */
public final class DefaultEncodingDeclarationEnricher extends AbstractAnnotatedFieldDeclarationEnricher {

  @Override
  protected ModelProperty getModelProperty(Field field) {
    return new DefaultEncodingModelProperty(field);
  }

  @Override
  protected Predicate<Field> getFieldHasAnnotationPredicate() {
    return ReflectionUtilsPredicates.<Field>withAnnotation(DefaultEncoding.class)
        .or(withAnnotation(com.zernova.sdk.api.annotation.param.DefaultEncoding.class));
  }

  @Override
  protected String getAnnotationName() {
    return "DefaultEncoding";
  }

  @Override
  protected Class getImplementingClass() {
    return String.class;
  }
}
