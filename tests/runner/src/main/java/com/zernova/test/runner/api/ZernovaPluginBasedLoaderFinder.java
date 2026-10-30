/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.runner.api;

import static com.zernova.runtime.core.api.util.IOUtils.closeQuietly;
import static com.zernova.runtime.module.artifact.activation.api.extension.discovery.boot.ExtensionLoaderUtils.getLoaderById;

import com.zernova.runtime.api.deployment.meta.ZernovaArtifactLoaderDescriptor;
import com.zernova.runtime.api.deployment.meta.ZernovaPluginModel;
import com.zernova.runtime.api.deployment.persistence.ZernovaPluginModelJsonSerializer;
import com.zernova.runtime.core.api.util.IOUtils;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ZernovaPluginBasedLoaderFinder {

  static final String META_INF_MULE_PLUGIN = "META-INF/mule-artifact.json";

  private static final ZernovaPluginModelJsonSerializer mulePluginSerializer = new ZernovaPluginModelJsonSerializer();
  private final ZernovaPluginModel mulePlugin;

  ZernovaPluginBasedLoaderFinder(InputStream json) {
    try {
      this.mulePlugin = mulePluginSerializer.deserialize(IOUtils.toString(json));
    } finally {
      closeQuietly(json);
    }
  }

  ZernovaPluginBasedLoaderFinder(File json) throws FileNotFoundException {
    this(new FileInputStream(json));
  }

  public Map<String, Object> getParams() {
    Map<String, Object> params = new HashMap<>();
    ZernovaArtifactLoaderDescriptor muleArtifactLoaderDescriptor = mulePlugin.getExtensionModelLoaderDescriptor().get();
    Map<String, Object> attributes = muleArtifactLoaderDescriptor.getAttributes();
    attributes.entrySet().stream()
        .forEach(entry -> {
          params.put(entry.getKey(), entry.getValue());
        });
    return params;
  }

  public boolean isExtensionModelLoaderDescriptorDefined() {
    return mulePlugin.getExtensionModelLoaderDescriptor().isPresent();
  }

  public ExtensionModelLoader getLoader() {
    return getLoaderById(mulePlugin.getExtensionModelLoaderDescriptor().get().getId());
  }

}
