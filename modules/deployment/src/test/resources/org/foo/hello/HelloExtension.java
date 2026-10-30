/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.hello;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.service.test.api.FooService;

import jakarta.inject.Inject;

/**
 * Extension for testing purposes
 */
@Extension(name = "Hello")
@Operations({HelloOperation.class})
public class HelloExtension {

  @Parameter
  private String message;
  @Inject
  private FooService fooService;

  @Inject
  private HelloRegistryBean registryBean;

  public HelloExtension() {}

  public String getMessage() {
    if (registryBean == null) {
      throw new NullPointerException("registryBean is null!");
    }
    return this.fooService.doFoo(this.message);
  }
}
