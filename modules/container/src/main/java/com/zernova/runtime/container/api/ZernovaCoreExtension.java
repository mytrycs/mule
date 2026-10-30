/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.api;

import com.zernova.runtime.api.meta.NamedObject;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;

/**
 * Allows Mule modules and transports to extend core functionality in an application-independent fashion.
 */
public interface ZernovaCoreExtension extends Lifecycle, NamedObject {

  /**
   * @param containerClassLoader container classloader which provides access to Mule API only. Non null
   */
  void setContainerClassLoader(ArtifactClassLoader containerClassLoader);

  /**
   * @return the priority (as an int), for the {@link ZernovaCoreExtension} to be loaded and executed. Override this method when
   *         wanting to ensure that an Extension is loaded before or after the rest. The lesser this number, the sooner it would
   *         be loaded.
   */
  default int priority() {
    return 1;
  }
}
