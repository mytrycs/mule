/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.ironman;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;

import java.util.Map;

import jakarta.inject.Inject;

/**
 * @since 1.7.0
 */
public class JarvisOperations {

  @Inject
  private CommsInterceptorVault vault;

  public void interceptComm(String issuer, Object comm) {
    vault.intercept(issuer, comm);
  }


  @OutputResolver(output = DisplayOutputResolver.class)
  public Map<String, Object> displayInterceptedComms() {
    return vault.display();
  }

  public static class DisplayOutputResolver implements OutputTypeResolver {

    @Override
    public String getCategoryName() {
      return "display";
    }

    @Override
    public MetadataType getOutputType(MetadataContext context, Object key) {
      return context.getTypeBuilder().arrayType().of(context.getTypeBuilder().objectType()).build();
    }

  }

}
