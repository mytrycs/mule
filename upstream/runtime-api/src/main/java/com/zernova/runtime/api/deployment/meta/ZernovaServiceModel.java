/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.deployment.meta;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.api.util.Preconditions.checkState;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.zernova.api.annotation.NoExtend;

import java.util.List;

/**
 * This object matches the zernova-artifact.json element within a service artifact. The describer holds information that has been
 * picked up from the JSON file (and the pom.xml when implemented).
 *
 * @since 1.0
 */
@NoExtend
public class ZernovaServiceModel extends AbstractZernovaArtifactModel {

  private static final String CONTRACT_CLASS_NAME = "contractClassName";
  private static final String SERVICE_PROVIDER_CLASS_NAME = "serviceProviderClassName";

  private List<ZernovaServiceContractModel> contracts;

  private ZernovaServiceModel() {
    // Nothing to do, this is just for allowing instantiation to GSON
  }

  private ZernovaServiceModel(String name, String minZernovaVersion, Product product,
                           ZernovaArtifactLoaderDescriptor classLoaderModelLoaderDescriptor,
                           ZernovaArtifactLoaderDescriptor bundleDescriptorLoader,
                           List<ZernovaServiceContractModel> contracts) {
    super(name, minZernovaVersion, product, classLoaderModelLoaderDescriptor, bundleDescriptorLoader);
    checkArgument(contracts != null && !contracts.isEmpty(), "service must fulfill at least one contract");

    this.contracts = unmodifiableList(contracts);
  }

  /**
   * @return Describes the contracts that are fulfilled by the elements in the described bundle
   * @since 1.2
   */
  public List<ZernovaServiceContractModel> getContracts() {
    return contracts;
  }

  @Override
  protected void doValidateCustomFields(String descriptorName) {
    checkState(contracts != null && !contracts.isEmpty(), "Service must fulfill at least one contract");
    contracts.forEach(contract -> {
      validateMandatoryFieldIsSet(CONTRACT_CLASS_NAME, contract.getContractClassName(), CONTRACT_CLASS_NAME);
      validateMandatoryFieldIsSet(SERVICE_PROVIDER_CLASS_NAME, contract.getServiceProviderClassName(),
                                  SERVICE_PROVIDER_CLASS_NAME);
    });
  }

  /**
   * A builder to create instances of {@link ZernovaServiceModel}.
   *
   * @since 1.0
   */
  public static class ZernovaServiceModelBuilder
      extends AbstractZernovaArtifactModelBuilder<ZernovaServiceModelBuilder, ZernovaServiceModel> {

    private List<ZernovaServiceContractModel> contracts = emptyList();

    @Override
    protected ZernovaServiceModelBuilder getThis() {
      return this;
    }

    public ZernovaServiceModelBuilder withContracts(List<ZernovaServiceContractModel> contracts) {
      this.contracts = contracts;
      return this;
    }

    /**
     * @return a well formed {@link ZernovaServiceModel}
     */
    @Override
    public ZernovaServiceModel build() {
      checkArgument(!isBlank(getName()), "name cannot be a blank");
      checkArgument(getMinZernovaVersion() != null, "minZernovaVersion cannot be null");
      checkArgument(getBundleDescriptorLoader() != null, "bundleDescriber cannot be null");

      return new ZernovaServiceModel(getName(), getMinZernovaVersion(),
                                  getRequiredProduct(),
                                  getClassLoaderModelDescriptorLoader(),
                                  getBundleDescriptorLoader(), contracts);
    }
  }
}
