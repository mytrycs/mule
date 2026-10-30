/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.launcher.coreextension;

import static com.zernova.runtime.api.util.IOUtils.getInputStreamWithCacheControl;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.core.api.util.PropertiesUtils.loadProperties;

import static java.lang.String.format;
import static java.util.Comparator.comparingInt;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.container.api.ZernovaCoreExtension;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;

import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers {@link ZernovaCoreExtension} classes that are defined in the classpath using core-extensions.properties files.
 */
public class ClasspathZernovaCoreExtensionDiscoverer implements ZernovaCoreExtensionDiscoverer {

  public static final String CORE_EXTENSION_RESOURCE_NAME =
      "META-INF/com/zernova/runtime/core/config/core-extensions.properties";

  private static Logger logger = LoggerFactory.getLogger(ClasspathZernovaCoreExtensionDiscoverer.class);
  private final ArtifactClassLoader containerClassLoader;

  /**
   * Creates a new extension discoverer
   *
   * @param containerClassLoader classloader where the discovering process will be executed. Non null.
   */
  public ClasspathZernovaCoreExtensionDiscoverer(ArtifactClassLoader containerClassLoader) {
    checkArgument(containerClassLoader != null, "Container classLoader cannot be null");
    this.containerClassLoader = containerClassLoader;
  }

  @Override
  public List<ZernovaCoreExtension> discover() throws ZernovaException {
    List<ZernovaCoreExtension> result = new LinkedList<>();

    Enumeration<?> e = ClassUtils.getResources(CORE_EXTENSION_RESOURCE_NAME, getClass().getClassLoader());
    List<Properties> extensions = new LinkedList<Properties>();

    // load ALL of the extension files first
    while (e.hasMoreElements()) {
      try {
        URL url = (URL) e.nextElement();
        if (logger.isDebugEnabled()) {
          logger.debug("Reading extension file: {}", url);
        }
        extensions.add(loadProperties(getInputStreamWithCacheControl(url)));
      } catch (Exception ex) {
        throw new DefaultZernovaException("Error loading Mule core extensions", ex);
      }
    }

    for (Properties extProps : extensions) {
      for (Map.Entry entry : extProps.entrySet()) {
        String extName = (String) entry.getKey();
        String extClass = (String) entry.getValue();
        try {
          ZernovaCoreExtension extension = (ZernovaCoreExtension) ClassUtils.instantiateClass(extClass);
          extension.setContainerClassLoader(containerClassLoader);
          result.add(extension);
        } catch (Throwable t) {
          throw new DefaultZernovaException(format("Error starting Mule core extension '%s'. Extension class is %s", extName,
                                                extClass),
                                         t);
        }
      }
    }
    result.sort(comparingInt(ZernovaCoreExtension::priority));
    return result;
  }
}
