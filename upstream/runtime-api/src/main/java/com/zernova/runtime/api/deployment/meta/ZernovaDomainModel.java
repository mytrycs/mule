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

@NoExtend
public class ZernovaDomainModel extends ZernovaDeployableModel {

  private ZernovaDomainModel() {
    // Nothing to do, this is just for allowing instantiation to GSON
  }

  private ZernovaDomainModel(String name,
                          String minMuleVersion,
                          Product product,
                          ZernovaArtifactLoaderDescriptor classLoaderModelLoaderDescriptor,
                          ZernovaArtifactLoaderDescriptor bundleDescriptor,
                          Set<String> configs,
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
  }

  /**
   * A builder to create instances of {@link ZernovaDomainModel}.
   *
   * @since 1.0
   */
  public static class ZernovaDomainModelBuilder
      extends ZernovaDeployableModelBuilder<ZernovaDomainModel.ZernovaDomainModelBuilder, ZernovaDomainModel> {

    @Override
    protected ZernovaDomainModelBuilder getThis() {
      return this;
    }

    @Override
    protected ZernovaDomainModel doCreateModel(Set<String> configs,
                                            Boolean redeploymentEnabled,
                                            List<String> secureProperties,
                                            Set<String> supportedJavaVersions,
                                            String logConfigFile) {
      return new ZernovaDomainModel(getName(),
                                 getMinMuleVersion(),
                                 getRequiredProduct(),
                                 getClassLoaderModelDescriptorLoader(),
                                 getBundleDescriptorLoader(),
                                 configs,
                                 ofNullable(redeploymentEnabled),
                                 secureProperties,
                                 supportedJavaVersions,
                                 logConfigFile);
    }
  }
}
