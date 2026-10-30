/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.classloader;

import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.core.api.exception.ResourceNotFoundException;

/**
 * Exception to be thrown within an artifact when a resource could not be found.
 *
 * @since 4.2
 */
public class ArtifactClassLoaderResourceNotFoundException extends ResourceNotFoundException {

  private final ClassLoaderNode classLoaderNode;

  public ArtifactClassLoaderResourceNotFoundException(I18nMessage message, ClassLoaderNode classLoaderNode) {
    super(message);
    this.classLoaderNode = classLoaderNode;
  }

  /**
   * @return a {@link ClassLoaderNode} which points to the
   *         {@link com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader} in context at the moment of the
   *         exception and provides information of the classloading hierarchy.
   */
  public ClassLoaderNode getClassLoaderNode() {
    return classLoaderNode;
  }
}
