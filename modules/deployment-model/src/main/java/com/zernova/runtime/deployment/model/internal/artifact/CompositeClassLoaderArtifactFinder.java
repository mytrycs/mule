/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.internal.artifact;

import com.zernova.runtime.core.internal.util.CompositeClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;

/**
 * Finds the first {@link ClassLoader} in an {@link CompositeClassLoader} that should be used when loading classes and resources
 *
 * @since 4.4, 4.3.1, 4.2.3
 */
public class CompositeClassLoaderArtifactFinder {

  private CompositeClassLoaderArtifactFinder() {
    // Private constructor to hide the implicit public one.
  }

  public static ClassLoader findClassLoader(CompositeClassLoader compositeClassLoader) {
    // Obtains the first artifact class loader that is not a plugin.
    for (ClassLoader delegate : compositeClassLoader.getDelegates()) {
      if (delegate instanceof ArtifactClassLoader && !isPluginClassLoader(delegate)) {
        return delegate;
      }
    }
    return compositeClassLoader.getDelegates().get(0);
  }

  private static boolean isPluginClassLoader(ClassLoader loggerClassLoader) {
    return ((ArtifactClassLoader) loggerClassLoader).getArtifactDescriptor() instanceof ArtifactPluginDescriptor;
  }

}
