/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.deployment.meta;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.api.service.ServiceProvider;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * Describes the {@link Service} contracts that are fulfilled by the owning {@link ZernovaServiceModel}
 *
 * @since 1.2
 */
@NoExtend
public class ZernovaServiceContractModel {

  private String serviceProviderClassName;
  private String contractClassName;

  private ZernovaServiceContractModel() {
    // Nothing to do, this is just for allowing instantiation to GSON
  }

  /**
   * Creates a new instance
   *
   * @param serviceProviderClassName The classname for the {@link ServiceProvider} that instantiates the contract implementation
   * @param contractClassName        the classname of the {@link Service} contract
   */
  public ZernovaServiceContractModel(String serviceProviderClassName, String contractClassName) {
    checkArgument(!StringUtils.isBlank(serviceProviderClassName), "serviceProviderClassName cannot be blank");
    checkArgument(!StringUtils.isBlank(contractClassName), "satisfiedServiceClassName cannot be blank");

    this.serviceProviderClassName = serviceProviderClassName;
    this.contractClassName = contractClassName;
  }

  /**
   * @return The classname for the {@link ServiceProvider} that instantiates the contract implementation
   */
  public String getServiceProviderClassName() {
    return serviceProviderClassName;
  }

  /**
   * @return The classname of the {@link Service} contract
   */
  public String getContractClassName() {
    return contractClassName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ZernovaServiceContractModel) {
      ZernovaServiceContractModel other = (ZernovaServiceContractModel) obj;
      return Objects.equals(serviceProviderClassName, other.getServiceProviderClassName()) &&
          Objects.equals(contractClassName, other.getContractClassName());
    }

    return false;
  }

  @Override
  public int hashCode() {
    return serviceProviderClassName.hashCode() * contractClassName.hashCode() * 11;
  }
}
