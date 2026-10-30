/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.exporter.config.impl;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.ENABLE_TRACER_CONFIGURATION_AT_APPLICATION_LEVEL_PROPERTY;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.SYSTEM_PROPERTY_PREFIX;
import static com.zernova.runtime.core.api.util.ClassUtils.getResourceOrFail;
import static com.zernova.runtime.core.api.util.IOUtils.getResourceAsStream;
import static com.zernova.runtime.core.api.util.IOUtils.getResourceAsUrl;
import static com.zernova.runtime.tracer.exporter.config.api.OpenTelemetrySpanExporterConfigurationProperties.MULE_OPEN_TELEMETRY_EXPORTER_CA_FILE_LOCATION;
import static com.zernova.runtime.tracer.exporter.config.api.OpenTelemetrySpanExporterConfigurationProperties.MULE_OPEN_TELEMETRY_EXPORTER_KEY_FILE_LOCATION;
import static com.zernova.runtime.tracer.exporter.config.api.OpenTelemetrySpanExporterConfigurationProperties.MULE_OPEN_TELEMETRY_TRACING_CONFIGURATION_FILE_PATH;

import static java.lang.Boolean.getBoolean;
import static java.lang.System.getProperty;
import static java.util.Optional.empty;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.config.api.properties.ConfigurationPropertiesResolver;
import com.zernova.runtime.config.internal.model.dsl.ClassLoaderResourceProvider;
import com.zernova.runtime.config.internal.model.dsl.config.DefaultConfigurationPropertiesResolver;
import com.zernova.runtime.config.internal.model.dsl.config.SystemPropertiesConfigurationProvider;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.module.observability.FileConfiguration;
import com.zernova.runtime.tracer.common.watcher.TracingConfigurationFileWatcher;
import com.zernova.runtime.tracer.exporter.config.api.SpanExporterConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A {@link SpanExporterConfiguration} based on a file in the conf folder.
 *
 * @since 4.5.0
 */
public class FileSpanExporterConfiguration extends FileConfiguration implements SpanExporterConfiguration, Disposable {

  private final ZernovaContext muleContext;

  public final static String MULE_TRACING_CONFIGURATION_PATH = SYSTEM_PROPERTY_PREFIX + "tracer.config.path";
  private final String PROPERTIES_FILE_NAME = getProperty(MULE_TRACING_CONFIGURATION_PATH, "tracer-exporter.conf");
  private ConfigurationPropertiesResolver propertyResolver;
  private JsonNode configuration;
  private ClassLoaderResourceProvider resourceProvider;
  private boolean propertiesInitialised;
  private URL configurationUrl;
  private final CompositeRunnable doOnConfigurationChanged = new CompositeRunnable();
  private TracingConfigurationFileWatcher tracingConfigurationFileWatcher;
  private String resolvedConfigurationFilePath;
  private boolean tracingConfigurationFileWatcherInitialised;

  public FileSpanExporterConfiguration(ZernovaContext muleContext) {
    super(muleContext);
    this.muleContext = muleContext;
  }

  @Override
  public String getStringValue(String key) {
    if (!propertiesInitialised) {
      initialiseProperties();
      propertiesInitialised = true;
    }

    return super.getStringValue(key);
  }

  protected boolean isAValueCorrespondingToAPath(String key) {
    return key.equals(MULE_OPEN_TELEMETRY_EXPORTER_CA_FILE_LOCATION) ||
        key.equals(MULE_OPEN_TELEMETRY_EXPORTER_KEY_FILE_LOCATION);
  }

  private void getSpanExporterConfigurationProperties() {
    if (getBoolean(ENABLE_TRACER_CONFIGURATION_AT_APPLICATION_LEVEL_PROPERTY)) {
      try {
        // This will verify first in the app and then in the conf folder.
        InputStream is = resourceProvider.getResourceAsStream(getPropertiesFileName());
        configuration = loadConfiguration(is);
        configurationUrl = getResourceOrFail(getPropertiesFileName(), getExecutionClassLoader(muleContext), true);
      } catch (ZernovaRuntimeException | IOException ignored) {
      }
    } else {
      try {
        String resourcePath = resolveConfigurationFilePath();
        InputStream is = getResourceAsStream(resourcePath, FileSpanExporterConfiguration.class);
        configuration = loadConfiguration(is);
        configurationUrl = getResourceAsUrl(resourcePath, FileSpanExporterConfiguration.class, true, true);
      } catch (IOException ignored) {
      }
    }
  }

  private String resolveConfigurationFilePath() {
    if (resolvedConfigurationFilePath != null) {
      return resolvedConfigurationFilePath;
    }
    resolvedConfigurationFilePath =
        getProperty(MULE_OPEN_TELEMETRY_TRACING_CONFIGURATION_FILE_PATH,
                    getConfFolder() + FileSystems.getDefault().getSeparator() + getPropertiesFileName());
    return resolvedConfigurationFilePath;
  }

  protected String getPropertiesFileName() {
    return PROPERTIES_FILE_NAME;
  }

  protected void initialiseProperties() {
    resourceProvider = new ClassLoaderResourceProvider(getExecutionClassLoader(muleContext));
    getSpanExporterConfigurationProperties();
    propertyResolver =
        new DefaultConfigurationPropertiesResolver(empty(),
                                                   new SystemPropertiesConfigurationProvider());
    if (configurationUrl != null && !tracingConfigurationFileWatcherInitialised) {
      tracingConfigurationFileWatcher = new TracingConfigurationFileWatcher(configurationUrl.getFile(), doOnConfigurationChanged);
      tracingConfigurationFileWatcher.start();
      tracingConfigurationFileWatcherInitialised = true;
    }
  }

  @Override
  public void doOnConfigurationChanged(Runnable doOnConfigurationChanged) {
    this.doOnConfigurationChanged.addRunnable(doOnConfigurationChanged);
  }

  @Override
  public void dispose() {
    if (tracingConfigurationFileWatcher != null) {
      tracingConfigurationFileWatcher.interrupt();
    }
  }

  @Override
  protected JsonNode getConfiguration() {
    return configuration;
  }

  @Override
  protected ConfigurationPropertiesResolver getPropertyResolver() {
    return propertyResolver;
  }

  private class CompositeRunnable implements Runnable {

    private final List<Runnable> runnables = new ArrayList<>();

    CompositeRunnable() {
      runnables.add(FileSpanExporterConfiguration.this::initialiseProperties);
    }

    @Override
    public void run() {
      runnables.forEach(Runnable::run);
    }

    public void addRunnable(Runnable runnable) {
      runnables.add(runnable);
    }
  }
}
