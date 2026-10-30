/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

import java.io.InputStream;

public class PetOwner {

  @Parameter
  String name;

  @Parameter
  InputStream signature;

  @Parameter
  @Optional
  TypedValue<InputStream> address;

  @Parameter
  @Optional
  @Alias("certificate")
  InputStream ownershipCertificate;

  public String getName() {
    return name;
  }

  public InputStream getSignature() {
    return signature;
  }

  public TypedValue<InputStream> getAddress() {
    return address;
  }

  public InputStream getOwnershipCertificate() {
    return ownershipCertificate;
  }
}
