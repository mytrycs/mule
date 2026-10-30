/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.deployment.meta;

import static java.util.Optional.ofNullable;

import com.zernova.api.annotation.NoExtend;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This object matches the mule-artifact.json element within a application. The describer holds information that has being picked
 * up from the JSON file (and the pom.xml when implemented).
 *
 * @since 1.0
 */
@NoExtend
public class ZernovaApplicationModel extends ZernovaDeployableModel {

  private String domain;

  private ZernovaApplicationModel() {
    // Nothing to do, this is just for allowing instantiation to GSON
  }

  private ZernovaApplicationModel(String name,
                               String minMuleVersion,
                               Product product,
                               ZernovaArtifactLoaderDescriptor classLoaderModelLoaderDescriptor,
                               ZernovaArtifactLoaderDescriptor bundleDescriptor,
                               Set<String> configs,
                               Optional<String> domain,
                               Optional<Boolean> redeploymentEnabled,
                               List<String> secureProperties,
                               Set<String> supportedJavaVersions,
                               String logConfigFile) {
    super(name,
          minMuleVersion,
          product,
          classLoaderModelLoaderDescriptor,
          bundleDescriptor,
          configs,
          redeploymentEnabled,
          secureProperties,
          supportedJavaVersions,
          logConfigFile);
    this.domain = domain.orElse(null);
  }

  /**
   * @return the domain associated with this application
   */
  public Optional<String> getDomain() {
    return ofNullable(domain);
  }

  /**
   * A builder to create instances of {@link ZernovaApplicationModel}.
   *
   * @since 1.0
   */
  public static class ZernovaApplicationModelBuilder
      extends ZernovaDeployableModelBuilder<ZernovaApplicationModelBuilder, ZernovaApplicationModel> {

    private String domain;

    @Override
    protected ZernovaApplicationModelBuilder getThis() {
      return this;
    }

    /**
     * @param domain the domain associated with this application
     * @return the same builder instance.
     */
    public ZernovaApplicationModelBuilder setDomain(String domain) {
      this.domain = domain;
      return this;
    }

    @Override
    protected ZernovaApplicationModel doCreateModel(Set<String> configs,
                                                 Boolean redeploymentEnabled,
                                                 List<String> secureProperties,
                                                 Set<String> supportedJavaVersions,
                                                 String logConfigFile) {
      return new ZernovaApplicationModel(getName(),
                                      getMinMuleVersion(),
                                      getRequiredProduct(),
                                      getClassLoaderModelDescriptorLoader(),
                                      getBundleDescriptorLoader(),
                                      configs,
                                      ofNullable(domain),
                                      ofNullable(redeploymentEnabled),
                                      secureProperties,
                                      supportedJavaVersions,
                                      logConfigFile);

    }
  }
}
