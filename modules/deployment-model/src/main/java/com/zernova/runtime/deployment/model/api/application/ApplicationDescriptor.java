/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.api.application;

import static java.util.Optional.empty;

import java.util.Optional;
import java.util.Properties;

/**
 * @deprecated since 4.5 use com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor instead.
 */
@Deprecated
public class ApplicationDescriptor extends com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor {

  /**
   * Creates a new application descriptor
   *
   * @param name application name. Non empty.
   */
  public ApplicationDescriptor(String name) {
    super(name, empty());
  }

  public ApplicationDescriptor(String name, Optional<Properties> properties) {
    super(name, properties);
  }

}
