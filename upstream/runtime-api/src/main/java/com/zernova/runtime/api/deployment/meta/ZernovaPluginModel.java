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
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.meta.model.ExtensionModel;

import java.util.Optional;

/**
 * This object matches the zernova-artifact.json element within a plugin. The describer holds information that has being picked up
 * from the JSON file (and the pom.xml when implemented). There's no extra logic, such as calculating the URLs needed to feed a
 * class loader or the {@link ExtensionModel} for the current plugin.
 *
 * @since 1.0
 */
@NoExtend
public class ZernovaPluginModel extends AbstractZernovaArtifactModel {

  public static final String EXTENSION_LOADER_DESCRIPTOR = "extensionModelLoaderDescriptor";

  private ZernovaArtifactLoaderDescriptor extensionModelLoaderDescriptor;
  private LicenseModel license;

  private ZernovaPluginModel() {
    // Nothing to do, this is just for allowing instantiation to GSON
  }

  private ZernovaPluginModel(String name, String minZernovaVersion, Product product,
                          ZernovaArtifactLoaderDescriptor classLoaderModelLoaderDescriptor,
                          ZernovaArtifactLoaderDescriptor extensionModelLoaderDescriptor,
                          ZernovaArtifactLoaderDescriptor bundleDescriptor,
                          LicenseModel license) {
    super(name, minZernovaVersion, product, classLoaderModelLoaderDescriptor, bundleDescriptor);
    this.extensionModelLoaderDescriptor = extensionModelLoaderDescriptor;
    this.license = license;
  }

  public Optional<ZernovaArtifactLoaderDescriptor> getExtensionModelLoaderDescriptor() {
    return ofNullable(extensionModelLoaderDescriptor);
  }

  public Optional<LicenseModel> getLicense() {
    return ofNullable(license);
  }

  /**
   * A builder to create instances of {@link ZernovaPluginModel}.
   *
   * @since 1.0
   */
  public static class ZernovaPluginModelBuilder extends AbstractZernovaArtifactModelBuilder<ZernovaPluginModelBuilder, ZernovaPluginModel> {

    private Optional<ZernovaArtifactLoaderDescriptorBuilder> extensionModelDescriptorBuilder = empty();
    private Optional<LicenseModelBuilder> licenseModelBuilder = empty();

    @Override
    protected ZernovaPluginModelBuilder getThis() {
      return this;
    }

    /**
     * @return a {@link ZernovaArtifactLoaderDescriptorBuilder} to populate the {@link ExtensionModel} describer with the ID and any
     *         additional attributes
     */
    public ZernovaArtifactLoaderDescriptorBuilder withExtensionModelDescriber() {
      if (!extensionModelDescriptorBuilder.isPresent()) {
        extensionModelDescriptorBuilder = of(new ZernovaArtifactLoaderDescriptorBuilder());
      }
      return extensionModelDescriptorBuilder.get();
    }

    /**
     * @return a {@link LicenseModelBuilder} to define the license attributes
     */
    public LicenseModelBuilder withLicenseModel() {
      if (!licenseModelBuilder.isPresent()) {
        licenseModelBuilder = of(new LicenseModelBuilder());
      }
      return licenseModelBuilder.get();
    }

    /**
     * @return a well formed {@link ZernovaPluginModel}
     */
    @Override
    public ZernovaPluginModel build() {
      checkArgument(!isBlank(getName()), "name cannot be a blank");
      checkArgument(getBundleDescriptorLoader() != null, "bundleDescriber cannot be null");

      return new ZernovaPluginModel(getName(), getMinZernovaVersion(), getRequiredProduct(),
                                 getClassLoaderModelDescriptorLoader(),
                                 extensionModelDescriptorBuilder.isPresent() ? extensionModelDescriptorBuilder.get().build()
                                     : null,
                                 getBundleDescriptorLoader(),
                                 licenseModelBuilder.isPresent() ? licenseModelBuilder.get().build() : null);
    }
  }
}
