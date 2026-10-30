/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.enricher;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.extension.api.exception.IllegalConfigurationModelDefinitionException;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.property.RuntimeVersionModelProperty;
import com.zernova.sdk.api.annotation.param.RuntimeVersion;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import org.reflections.util.ReflectionUtilsPredicates;

/**
 * {@link DeclarationEnricher} which looks at classes with fields annotated with {@link RuntimeVersion}. It validates that the
 * annotations is used properly and if so it adds a {@link RuntimeVersionModelProperty}.
 * <p>
 * If the {@link RuntimeVersion} annotation is used in a way which breaks the rules set on its javadoc, an
 * {@link IllegalConfigurationModelDefinitionException} will be thrown.
 *
 * @since 4.4
 */
public class RuntimeVersionDeclarationEnricher extends AbstractAnnotatedFieldDeclarationEnricher {

  @Override
  protected ModelProperty getModelProperty(Field field) {
    return new RuntimeVersionModelProperty(field);
  }

  @Override
  protected Predicate<Field> getFieldHasAnnotationPredicate() {
    return ReflectionUtilsPredicates.<Field>withAnnotation(RuntimeVersion.class);
  }

  @Override
  protected String getAnnotationName() {
    return "RuntimeVersion";
  }

  @Override
  protected Class getImplementingClass() {
    return ZernovaVersion.class;
  }
}
