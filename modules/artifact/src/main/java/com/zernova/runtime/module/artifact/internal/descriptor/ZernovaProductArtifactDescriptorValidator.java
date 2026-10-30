/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.internal.descriptor;

import static com.zernova.runtime.api.deployment.meta.Product.getProductByName;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;

import static java.lang.String.format;

import com.zernova.runtime.api.deployment.meta.Product;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorCreateException;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidator;

/**
 * Validator for {@link ArtifactDescriptor#getRequiredProduct()}.
 *
 * @since 4.1
 */
public class ZernovaProductArtifactDescriptorValidator implements ArtifactDescriptorValidator {

  @Override
  public void validate(ArtifactDescriptor descriptor) {
    Product requiredProduct = descriptor.getRequiredProduct();
    Product runtimeProduct = getProductByName(getMuleManifest().getProductName());
    if (!runtimeProduct.supports(requiredProduct)) {
      throw new ArtifactDescriptorCreateException(format("The artifact %s requires a different runtime. The artifact required runtime is %s and the runtime is %s",
                                                         descriptor.getName(), descriptor.getRequiredProduct().name(),
                                                         runtimeProduct.name()));
    }
  }

}
