/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.api.discoverer;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

import com.zernova.runtime.container.internal.ContainerModuleDiscoverer;

/**
 * Utils to allow test-runner to use the module discovery mechanism for the Mule container.
 *
 * @since 4.6
 */
public final class ContainerDiscovererHelper {

  private ContainerDiscovererHelper() {
    // nothing to do
  }

  /**
   * Allows test-runner to use the module discovery mechanism for the Mule container.
   */
  public static void exportInternalsToTestRunner() {
    // Make sure only allowed users within the Mule Runtime use this
    final String callerClassName = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).getCallerClass().getName();
    if (!(callerClassName.equals("com.zernova.test.runner.classloader.container.TestPreFilteredContainerClassLoaderCreator"))) {
      throw new UnsupportedOperationException("This is for internal use only.");
    }

    ContainerDiscovererHelper.class.getModule()
        .addExports(ContainerModuleDiscoverer.class.getPackageName(),
                    StackWalker.getInstance(RETAIN_CLASS_REFERENCE).getCallerClass().getModule());

  }
}
