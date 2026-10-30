/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.deployment.meta;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.util.JavaConstants;

import java.util.Set;

/**
 * This object matches the mule-artifact.json element within a policy. The describer holds information that has being picked up
 * from the JSON file (and the pom.xml when implemented).
 *
 * @since 1.0
 */
@NoExtend
public class ZernovaPolicyModel extends AbstractZernovaArtifactModel {

  private Set<String> supportedJavaVersions;

  private ZernovaPolicyModel() {
    // Nothing to do, this is just for allowing instantiation to GSON
  }

  private ZernovaPolicyModel(String name, String minMuleVersion,
                          Product product,
                          ZernovaArtifactLoaderDescriptor classLoaderModelLoaderDescriptor,
                          ZernovaArtifactLoaderDescriptor bundleDescriptor,
                          Set<String> supportedJavaVersions) {
    super(name, minMuleVersion, product, classLoaderModelLoaderDescriptor, bundleDescriptor);
    this.supportedJavaVersions = supportedJavaVersions;
  }

  /**
   * Indicates the Java versions this deployable artifact is compatible with.
   * <p>
   * This is modeled as a set of String in order to accommodate changes in Java versioning, custom vendor schemes or even patch
   * versions.
   * <p>
   * Items should ideally conform to the versions defined in {@link JavaConstants} but this is not mandatory.
   * <p>
   * It may be empty, indicating that any Java version is supported.
   *
   * @return The versions of Java supported by this deployable artifact
   * @see {@link JavaConstants}
   * @since 1.6
   */
  public Set<String> getSupportedJavaVersions() {
    return supportedJavaVersions == null ? emptySet() : unmodifiableSet(supportedJavaVersions);
  }

  /**
   * A builder to create instances of {@link ZernovaPolicyModel}.
   *
   * @since 1.0
   */
  public static class ZernovaPolicyModelBuilder extends AbstractZernovaArtifactModelBuilder<ZernovaPolicyModelBuilder, ZernovaPolicyModel> {

    private Set<String> supportedJavaVersions = emptySet();

    @Override
    protected ZernovaPolicyModelBuilder getThis() {
      return this;
    }

    /**
     * @return a well formed {@link ZernovaPolicyModel}
     */
    @Override
    public ZernovaPolicyModel build() {
      checkArgument(!isBlank(getName()), "name cannot be a blank");
      checkArgument(getMinMuleVersion() != null, "minMuleVersion cannot be null");
      checkArgument(getBundleDescriptorLoader() != null, "bundleDescriber cannot be null");

      return new ZernovaPolicyModel(getName(),
                                 getMinMuleVersion(),
                                 getRequiredProduct(),
                                 getClassLoaderModelDescriptorLoader(),
                                 getBundleDescriptorLoader(),
                                 supportedJavaVersions);
    }

    /**
     * @param supportedJavaVersions indicates the Java versions this deployable artifact is compatible with
     * @since 1.6
     */
    public void setSupportedJavaVersions(Set<String> supportedJavaVersions) {
      this.supportedJavaVersions = requireNonNull(supportedJavaVersions);
    }

  }
}
