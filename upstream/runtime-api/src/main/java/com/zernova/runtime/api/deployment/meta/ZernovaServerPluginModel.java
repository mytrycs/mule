/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.deployment.meta;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.zernova.api.annotation.NoExtend;

import java.util.Optional;

/**
 * This object matches the mule-artifact.json element within a server plugin. The describer holds information that has been picked
 * up from the JSON file (and the pom.xml when implemented).
 *
 * @since 1.0
 */
@NoExtend
public class ZernovaServerPluginModel extends AbstractZernovaArtifactModel {

  private static final String PLUGIN_CLASS_NAME = "pluginClassName";

  private ZernovaArtifactLoaderDescriptor extensionModelLoaderDescriptor;
  private String pluginClassName;

  private ZernovaServerPluginModel() {
    // Nothing to do, this is just for allowing instantiation to GSON
  }

  private ZernovaServerPluginModel(String name, String minMuleVersion, Product product,
                                ZernovaArtifactLoaderDescriptor classLoaderModelLoaderDescriptor,
                                ZernovaArtifactLoaderDescriptor extensionModelLoaderDescriptor,
                                ZernovaArtifactLoaderDescriptor bundleDescriptor, String pluginClassName) {
    super(name, minMuleVersion, product, classLoaderModelLoaderDescriptor, bundleDescriptor);
    this.extensionModelLoaderDescriptor = extensionModelLoaderDescriptor;
    this.pluginClassName = pluginClassName;
  }

  public Optional<ZernovaArtifactLoaderDescriptor> getExtensionModelLoaderDescriptor() {
    return ofNullable(extensionModelLoaderDescriptor);
  }

  public String getPluginClassName() {
    return pluginClassName;
  }

  @Override
  protected void doValidateCustomFields(String descriptorName) {
    validateMandatoryFieldIsSet(descriptorName, pluginClassName, PLUGIN_CLASS_NAME);
  }

  /**
   * A builder to create instances of {@link ZernovaServerPluginModel}.
   *
   * @since 1.0
   */
  public static class ZernovaServerPluginModelBuilder
      extends AbstractZernovaArtifactModelBuilder<ZernovaServerPluginModelBuilder, ZernovaServerPluginModel> {

    private Optional<ZernovaArtifactLoaderDescriptorBuilder> extensionModelDescriptorBuilder = empty();
    private String pluginClassName;

    @Override
    protected ZernovaServerPluginModelBuilder getThis() {
      return this;
    }

    /**
     * @param className name of the class implementing the server plugin
     * @return same builder instance
     */
    public ZernovaServerPluginModelBuilder withPluginClassName(String className) {
      this.pluginClassName = className;

      return this;
    }

    /**
     * @return a well formed {@link ZernovaServerPluginModel}
     */
    @Override
    public ZernovaServerPluginModel build() {
      checkArgument(!isBlank(getName()), "name cannot be a blank");
      checkArgument(!isBlank(pluginClassName), "pluginClassName cannot be blank");
      checkArgument(getMinMuleVersion() != null, "minMuleVersion cannot be null");
      checkArgument(getBundleDescriptorLoader() != null, "bundleDescriber cannot be null");

      return new ZernovaServerPluginModel(getName(), getMinMuleVersion(), getRequiredProduct(),
                                       getClassLoaderModelDescriptorLoader(),
                                       extensionModelDescriptorBuilder.isPresent() ? extensionModelDescriptorBuilder.get().build()
                                           : null,
                                       getBundleDescriptorLoader(), pluginClassName);
    }
  }
}
