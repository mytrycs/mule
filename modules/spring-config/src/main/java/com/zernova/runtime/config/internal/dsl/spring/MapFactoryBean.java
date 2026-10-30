/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.spring;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.dsl.api.component.MapEntry;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;
import java.util.Map;

/**
 * {@code FactoryBean} that creates a {@code Map} from a collection of {@code MapEntry} and the type of the map.
 */
public class MapFactoryBean extends AbstractComponent implements FactoryBean {

  private final List<MapEntry> mapEntries;
  private final Class<? extends Map> mapType;

  /**
   * @param mapEntries the collection of entries to store in the map
   * @param mapType    the map type
   */
  public MapFactoryBean(List<MapEntry> mapEntries, Class<? extends Map> mapType) {
    this.mapEntries = mapEntries;
    this.mapType = mapType;
  }

  @Override
  public Map getObject() throws Exception {
    Map map = ClassUtils.instantiateClass(mapType);
    for (MapEntry mapEntry : mapEntries) {
      map.put(mapEntry.getKey(), mapEntry.getValue());
    }
    return map;
  }

  @Override
  public Class<?> getObjectType() {
    return Map.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
