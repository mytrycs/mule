/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.property;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.module.extension.internal.lifecycle.ArtifactLifecycleListenerFactory;
import com.zernova.sdk.api.artifact.lifecycle.ArtifactLifecycleListener;

/**
 * A model property which gives access to an {@link ArtifactLifecycleListenerFactory} that can be used to create instances of a
 * given {@link ArtifactLifecycleListener} class.
 *
 * @since 4.5.0
 */
public class ArtifactLifecycleListenerModelProperty implements ModelProperty {

  private final ArtifactLifecycleListenerFactory artifactLifecycleListenerFactory;

  public ArtifactLifecycleListenerModelProperty(Class<? extends ArtifactLifecycleListener> artifactLifecycleListenerClass) {
    this.artifactLifecycleListenerFactory = new ArtifactLifecycleListenerFactory(artifactLifecycleListenerClass);
  }

  @Override
  public String getName() {
    return "artifactLifecycleListenerModelProperty";
  }

  @Override
  public boolean isPublic() {
    return false;
  }

  /**
   * @return the extension's {@link ArtifactLifecycleListenerFactory}.
   */
  public ArtifactLifecycleListenerFactory getArtifactLifecycleListenerFactory() {
    return artifactLifecycleListenerFactory;
  }
}
