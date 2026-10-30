/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.goodbye;

import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

/**
 * Extension for testing purposes
 */
@Extension(name = "Goodbye")
@Configurations({GoodByeConfiguration.class})
@Xml(prefix = "goodbye")
public class GoodByeExtension {

  @Parameter
  private String message;

  public GoodByeExtension() {
  }

}
