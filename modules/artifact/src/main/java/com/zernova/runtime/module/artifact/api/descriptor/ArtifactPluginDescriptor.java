/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.descriptor;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.deployment.meta.LicenseModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.module.artifact.api.plugin.LoaderDescriber;

import java.util.Optional;
import java.util.Properties;

/**
 * Describes an artifact with classifier {@code zernova-plugin}, used within an Application or Domain.
 *
 * @since 4.5
 */
@NoExtend
public class ArtifactPluginDescriptor extends ArtifactDescriptor {

  private static final String META_INF = "META-INF";
  public static final String MULE_PLUGIN_CLASSIFIER = BundleDescriptor.MULE_PLUGIN_CLASSIFIER;
  public static final String EXTENSION_BUNDLE_TYPE = "jar";
  private static final String MULE_ARTIFACT = "zernova-artifact";

  /**
   * Target path as URL for any files used at deployment time or when generating the {@link ExtensionModel}
   */
  public static final String MULE_ARTIFACT_PATH_INSIDE_JAR = META_INF + "/" + MULE_ARTIFACT;
  public static final String MULE_AUTO_GENERATED_ARTIFACT_PATH_INSIDE_JAR =
      META_INF + "/auto-generated-" + MULE_ARTIFACT_JSON_DESCRIPTOR;;
  public static final String MULE_PLUGIN_POM = "pom.xml";

  private Optional<LoaderDescriber> extensionModelDescriptorProperty = empty();
  private Optional<LicenseModel> licenseModel = empty();

  /**
   * Creates a new artifact plugin descriptor
   *
   * @param name artifact plugin name. Non empty.
   */
  public ArtifactPluginDescriptor(String name) {
    super(name);
  }

  /**
   * @return the {@link LoaderDescriber} that will contain all mandatory values to generate an {@link ExtensionModel} from it.
   */
  public Optional<LoaderDescriber> getExtensionModelDescriptorProperty() {
    return extensionModelDescriptorProperty;
  }

  /**
   * Takes a {@link LoaderDescriber} that should contain the values used to properly initialize an {@link ExtensionModel}
   *
   * @param extensionModelLoaderDescriber the {@link LoaderDescriber} with the values
   */
  public void setExtensionModelDescriptorProperty(LoaderDescriber extensionModelLoaderDescriber) {
    this.extensionModelDescriptorProperty = ofNullable(extensionModelLoaderDescriber);
  }

  /**
   * @return the license requirements for this plugin. Empty if there are not license requirements.
   */
  public Optional<LicenseModel> getLicenseModel() {
    return licenseModel;
  }

  /**
   * @param licenseModel the license requirements for this plugin.
   */
  public void setLicenseModel(LicenseModel licenseModel) {
    this.licenseModel = of(requireNonNull(licenseModel, "licenseModel cannot be null"));
  }
}
