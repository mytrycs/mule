/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.policyIsolation;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import org.foo.policyIsolation.internal.InternalRegistryBean;

import jakarta.inject.Inject;

public class PolicyDependencyInjectionOperations {

  @Inject
  private InternalRegistryBean registryBean;

  @MediaType(value = TEXT_PLAIN, strict = false)
  public String injectionCheck(@Config PolicyDependencyInjectionExtension config) throws Exception {
    config.checkExtensionInjection();
    if (registryBean == null) {
      throw new NullPointerException("registryBean is null (has not been injected into the extension operations)");
    }
    return "registryBean has been injected into the extension, its operations and it's functions";
  }

}
