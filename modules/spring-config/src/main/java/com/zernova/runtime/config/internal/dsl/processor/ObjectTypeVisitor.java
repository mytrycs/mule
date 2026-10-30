/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.processor;

import static java.lang.Thread.currentThread;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.dsl.api.component.TypeDefinition;
import com.zernova.runtime.dsl.api.component.TypeDefinitionVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;

/**
 * Visitor that retrieves the {@code ComponentModel} object {@code Class} based on the component configuration.
 *
 * @since 4.0
 */
public class ObjectTypeVisitor implements TypeDefinitionVisitor {

  public static final Class<ArrayList> DEFAULT_COLLECTION_TYPE = ArrayList.class;
  private static final Class<HashMap> DEFAULT_MAP_TYPE = HashMap.class;
  private static final Class<HashSet> DEFAULT_SET_CLASS = HashSet.class;

  private final ComponentAst componentModel;
  private Class<?> type;
  private Optional<TypeDefinition.MapEntryType> mapEntryType = empty();

  public ObjectTypeVisitor(ComponentAst componentModel) {
    this.componentModel = componentModel;
  }

  @Override
  public void onType(Class<?> type) {
    this.type = resolveType(type);
  }

  private Class<?> resolveType(Class<?> type) {
    if (Collection.class.equals(type) || List.class.equals(type)) {
      return DEFAULT_COLLECTION_TYPE;
    } else if (Set.class.equals(type)) {
      return DEFAULT_SET_CLASS;
    } else if (Map.class.equals(type)) {
      return DEFAULT_MAP_TYPE;
    } else {
      return type;
    }
  }

  @Override
  public void onConfigurationAttribute(String attributeName, Class<?> enforcedClass) {
    onConfigurationAttribute(DEFAULT_GROUP_NAME, attributeName, enforcedClass);
  }

  @Override
  public void onConfigurationAttribute(String groupName, String attributeName, Class<?> enforcedClass) {
    String attributeValue = componentModel.getParameter(groupName, attributeName).getResolvedRawValue();
    try {
      type = ClassUtils.getClass(currentThread().getContextClassLoader(),
                                 attributeValue);
      if (!enforcedClass.isAssignableFrom(type)) {
        throw new ZernovaRuntimeException(createStaticMessage("Class definition for type %s on element %s is not the same nor inherits from %s",
                                                           attributeValue,
                                                           componentModel.getIdentifier(), enforcedClass.getName()));
      }
    } catch (ClassNotFoundException e) {
      throw new ZernovaRuntimeException(createStaticMessage("Error while trying to locate Class definition for type %s on element %s",
                                                         attributeValue,
                                                         componentModel.getIdentifier()),
                                     e);
    }
  }

  @Override
  public void onMapType(TypeDefinition.MapEntryType mapEntryType) {
    this.type = mapEntryType.getClass();
    this.mapEntryType =
        of(new TypeDefinition.MapEntryType(resolveType(mapEntryType.getKeyType()), resolveType(mapEntryType.getValueType())));
  }

  public Class<?> getType() {
    return type;
  }

  public Optional<TypeDefinition.MapEntryType> getMapEntryType() {
    return mapEntryType;
  }
}
