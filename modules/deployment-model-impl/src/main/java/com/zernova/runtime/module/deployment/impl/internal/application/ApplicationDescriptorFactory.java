/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.application;

import static com.zernova.runtime.api.artifact.ArtifactType.APP;
import static com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor.DEFAULT_CONFIGURATION_RESOURCE;

import com.zernova.runtime.api.artifact.ArtifactType;
import com.zernova.runtime.api.deployment.meta.ZernovaApplicationModel;
import com.zernova.runtime.api.deployment.persistence.AbstractZernovaArtifactModelJsonSerializer;
import com.zernova.runtime.api.deployment.persistence.ZernovaApplicationModelJsonSerializer;
import com.zernova.runtime.core.api.util.PropertiesUtils;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import com.zernova.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import com.zernova.runtime.module.deployment.impl.internal.artifact.AbstractDeployableDescriptorFactory;
import com.zernova.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Creates artifact descriptor for application
 */
public class ApplicationDescriptorFactory
    extends AbstractDeployableDescriptorFactory<ZernovaApplicationModel, ApplicationDescriptor> {

  public static final String SYSTEM_PROPERTY_OVERRIDE = "-O";

  public ApplicationDescriptorFactory(ArtifactPluginDescriptorLoader artifactPluginDescriptorLoader,
                                      DescriptorLoaderRepository descriptorLoaderRepository,
                                      ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder) {
    super(artifactPluginDescriptorLoader, descriptorLoaderRepository, artifactDescriptorValidatorBuilder);
  }

  @Override
  protected void doDescriptorConfig(ZernovaApplicationModel artifactModel, ApplicationDescriptor descriptor,
                                    File artifactLocation) {
    super.doDescriptorConfig(artifactModel, descriptor, artifactLocation);
    artifactModel.getDomain().ifPresent(descriptor::setDomainName);
  }

  @Override
  protected ApplicationDescriptor createArtifactDescriptor(File artifactLocation, String name,
                                                           Optional<Properties> deploymentProperties) {
    // Keep compatibility with usages of the factory that expect the descriptor from previous version.
    return new com.zernova.runtime.deployment.model.api.application.ApplicationDescriptor(artifactLocation.getName(),
                                                                                       deploymentProperties);
  }

  @Override
  protected String getDefaultConfigurationResource() {
    return DEFAULT_CONFIGURATION_RESOURCE;
  }

  @Override
  protected ArtifactType getArtifactType() {
    return APP;
  }

  @Override
  protected AbstractZernovaArtifactModelJsonSerializer<ZernovaApplicationModel> getMuleArtifactModelJsonSerializer() {
    return new ZernovaApplicationModelJsonSerializer();
  }

  void setApplicationProperties(ApplicationDescriptor desc, File appPropsFile) {
    // ugh, no straightforward way to convert a HashTable to a map
    Map<String, String> m = new HashMap<>();

    if (appPropsFile.exists() && appPropsFile.canRead()) {
      final Properties props;
      try {
        props = PropertiesUtils.loadProperties(appPropsFile.toURI().toURL());
      } catch (IOException e) {
        throw new IllegalArgumentException("Unable to obtain application properties file URL", e);
      }
      for (Object key : props.keySet()) {
        m.put(key.toString(), props.getProperty(key.toString()));
      }
    }

    // Override with any system properties prefixed with "-O" for ("override"))
    Properties sysProps = System.getProperties();
    for (Map.Entry<Object, Object> entry : sysProps.entrySet()) {
      String key = entry.getKey().toString();
      if (key.startsWith(SYSTEM_PROPERTY_OVERRIDE)) {
        m.put(key.substring(SYSTEM_PROPERTY_OVERRIDE.length()), entry.getValue().toString());
      }
    }
    desc.setAppProperties(m);
  }

}
