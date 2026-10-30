/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.http;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

/**
 * Extension class for the test HTTP plugin using org.foo.http package.
 */
@Xml(prefix = "foo-http")
@Extension(name = "HTTP")
@Operations(HttpOperations.class)
public class HttpExtension {

  @Parameter
  private String message;

  /**
   * Getter method for the 'message' parameter.
   * This is the method HttpOperations is trying to call.
   * @return The configured message.
   */
  public String getMessage() {
    return (message != null) ? message : "Default Message";
  }


}