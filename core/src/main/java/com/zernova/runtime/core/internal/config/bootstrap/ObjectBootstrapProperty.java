/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config.bootstrap;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.api.config.bootstrap.BootstrapService;
import com.zernova.runtime.core.api.util.StringUtils;

import java.util.Set;

/**
 * Defines a bootstrap property for a generic object
 */
public class ObjectBootstrapProperty extends AbstractBootstrapProperty {

  private final String key;
  private final String className;

  /**
   * Creates a generic bootstrap property
   *
   * @param service       service that provides the property. Not null.
   * @param artifactTypes defines what is the artifact this bootstrap object applies to
   * @param key           key used to register the object. Not empty.
   * @param className     className of the bootstrapped object. Not empty.
   */
  public ObjectBootstrapProperty(BootstrapService service, Set<ArtifactType> artifactTypes, String key,
                                 String className) {
    super(service, artifactTypes);
    checkArgument(!StringUtils.isEmpty(key), "key cannot be empty");
    checkArgument(!StringUtils.isEmpty(className), "className cannot be empty");

    this.key = key;
    this.className = className;
  }

  public String getKey() {
    return key;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public String toString() {
    return String.format("Object{ %s}", className);
  }
}
