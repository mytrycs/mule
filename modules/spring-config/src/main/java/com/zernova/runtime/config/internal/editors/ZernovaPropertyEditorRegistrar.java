/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.editors;

import static com.zernova.runtime.api.util.IOUtils.getInputStreamWithCacheControl;
import static com.zernova.runtime.core.api.util.IOUtils.closeQuietly;
import static com.zernova.runtime.module.artifact.activation.internal.classloader.ZernovaApplicationClassLoader.resolveContextArtifactPluginClassLoaders;

import java.beans.PropertyEditor;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

/**
 * The preferred way to configure property editors in Spring 2/3 is to implement a registrar
 */
public class ZernovaPropertyEditorRegistrar implements PropertyEditorRegistrar {

  private Map<Class<?>, Class<PropertyEditor>> customPropertyEditorsCache;
  private static final String CUSTOM_PROPERTY_EDITOR_RESOURCE_NAME = "META-INF/mule.custom-property-editors";

  private final DatePropertyEditor datePropertyEditor = new DatePropertyEditor(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"),
                                                                               new SimpleDateFormat("yyyy-MM-dd"), true);

  @Override
  public void registerCustomEditors(PropertyEditorRegistry registry) {
    registry.registerCustomEditor(Date.class, datePropertyEditor);

    if (customPropertyEditorsCache == null) {
      discoverCustomPropertyEditor();
    }
    for (Map.Entry<Class<?>, Class<PropertyEditor>> entry : customPropertyEditorsCache.entrySet()) {
      try {
        final PropertyEditor customEditor = entry.getValue().getConstructor().newInstance();
        registry.registerCustomEditor(entry.getKey(), customEditor);
      } catch (Exception e) {
        throw new IllegalStateException("Error loading custom property editors", e);
      }
    }

  }

  private void discoverCustomPropertyEditor() {
    customPropertyEditorsCache = new HashMap<>();

    for (ClassLoader classLoader : resolveContextArtifactPluginClassLoaders()) {
      // Look for any editors needed by extensions
      try {
        Enumeration<URL> urls = classLoader.getResources(CUSTOM_PROPERTY_EDITOR_RESOURCE_NAME);
        while (urls.hasMoreElements()) {
          URL url = urls.nextElement();
          Properties props = new Properties();
          InputStream stream = getInputStreamWithCacheControl(url);
          try {
            props.load(stream);
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
              String target = (String) entry.getKey();
              String editor = (String) entry.getValue();
              Class<?> requiredType = classLoader.loadClass(target);
              Class<PropertyEditor> propertyEditorClass = (Class<PropertyEditor>) classLoader.loadClass(editor);
              customPropertyEditorsCache.put(requiredType, propertyEditorClass);
            }
          } finally {
            closeQuietly(stream);
          }
        }
      } catch (Exception e) {
        throw new IllegalStateException("Error loading custom property editors", e);
      }
    }
  }
}
