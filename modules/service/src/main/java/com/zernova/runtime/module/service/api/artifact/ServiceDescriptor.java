/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.api.artifact;

import static java.util.Collections.emptyList;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.api.deployment.meta.ZernovaServiceContractModel;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

import java.util.List;

/**
 * Describes how to create a {@link Service} instance.
 */
public class ServiceDescriptor extends ArtifactDescriptor {

  private List<ZernovaServiceContractModel> contractModels = emptyList();

  /**
   * Creates a new service descriptor
   *
   * @param name service name. Non empty.
   */
  public ServiceDescriptor(String name) {
    super(name);
  }

  /**
   * @return the {@link ZernovaServiceContractModel} that are fulfilled by the service artifact.
   */
  public List<ZernovaServiceContractModel> getContractModels() {
    return contractModels;
  }

  /**
   * Sets the {@link ZernovaServiceContractModel} that are fulfilled by the service artifact.
   *
   * @param contractModels the {@link ZernovaServiceContractModel} that are fulfilled by the service artifact.
   */
  public void setContractModels(List<ZernovaServiceContractModel> contractModels) {
    checkArgument(!contractModels.isEmpty(), "contractModels cannot be empty");
    this.contractModels = contractModels;
  }
}
