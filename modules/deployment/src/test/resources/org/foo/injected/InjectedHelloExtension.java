/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.injected;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Extension for testing purposes
 */
@Extension(name = "Hello")
@Operations({InjectedHelloOperation.class})
public class InjectedHelloExtension {

  @Parameter
  private String message;

  @Inject
  @Named("plugin.echotest")
  private Object echoTest;


  public String getMessage() {
    return message + echoTest.toString();
  }
}
