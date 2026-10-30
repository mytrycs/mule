/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.lifecycle;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.sdk.api.artifact.lifecycle.ArtifactLifecycleListener;

/**
 * Factory class for creating {@link ArtifactLifecycleListener} instances.
 *
 * @since 4.5.0
 */
public class ArtifactLifecycleListenerFactory {

  private final Class<? extends ArtifactLifecycleListener> clazz;

  /**
   * Creates a factory for the given {@code artifactLifecycleListenerClass}.
   *
   * @param artifactLifecycleListenerClass the corresponding {@link ArtifactLifecycleListener} class that should be instantiated
   *                                       by this factory.
   */
  public ArtifactLifecycleListenerFactory(Class<? extends ArtifactLifecycleListener> artifactLifecycleListenerClass) {
    checkArgument(artifactLifecycleListenerClass != null, "ArtifactLifecycleListener type cannot be null");
    this.clazz = artifactLifecycleListenerClass;
  }

  /**
   * @return the {@link ArtifactLifecycleListener} instance of the corresponding class.
   */
  public ArtifactLifecycleListener createArtifactLifecycleListener() {
    try {
      return ClassUtils.instantiateClass(clazz);
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create ArtifactLifecycleListener of type %s",
                                                         clazz.getName()),
                                     e);
    }
  }
}
