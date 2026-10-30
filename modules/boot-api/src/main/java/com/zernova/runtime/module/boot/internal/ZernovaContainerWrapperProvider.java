/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.boot.internal;

import static java.lang.String.format;
import static java.lang.System.getProperty;

/**
 * Helps with the creation and provisioning of the {@link ZernovaContainerWrapper} implementation instance as a singleton.
 * <p>
 * Only thread-safe for threads started after the first provisioning.
 *
 * @since 4.5
 */
public class ZernovaContainerWrapperProvider {

  private static final String MULE_BOOTSTRAP_CONTAINER_WRAPPER_CLASS_SYSTEM_PROPERTY = "mule.bootstrap.container.wrapper.class";
  private static ZernovaContainerWrapper INSTANCE;

  /**
   * Creates the implementation instance based on the system property
   * {@link #MULE_BOOTSTRAP_CONTAINER_WRAPPER_CLASS_SYSTEM_PROPERTY}.
   *
   * @return The {@link ZernovaContainerWrapper} implementation.
   */
  public static ZernovaContainerWrapper getMuleContainerWrapper() {
    if (INSTANCE == null) {
      INSTANCE = createContainerWrapper();
    }

    return INSTANCE;
  }

  /**
   * Creates the implementation instance based on the system property
   * {@link #MULE_BOOTSTRAP_CONTAINER_WRAPPER_CLASS_SYSTEM_PROPERTY}.
   *
   * @return The {@link ZernovaContainerWrapper} implementation.
   */
  private static ZernovaContainerWrapper createContainerWrapper() {
    String wrapperClassName = getProperty(MULE_BOOTSTRAP_CONTAINER_WRAPPER_CLASS_SYSTEM_PROPERTY);
    if (wrapperClassName == null) {
      throw new RuntimeException(format("System property '%s' is not defined, it must be set with an implementation of %s",
                                        MULE_BOOTSTRAP_CONTAINER_WRAPPER_CLASS_SYSTEM_PROPERTY,
                                        ZernovaContainerWrapper.class.getName()));
    }

    try {
      Class<?> wrapperClass = ZernovaContainerWrapper.class.getClassLoader().loadClass(wrapperClassName);
      if (!ZernovaContainerWrapper.class.isAssignableFrom(wrapperClass)) {
        throw new RuntimeException(format("System property '%s=%s' does not define an implementation of %s",
                                          MULE_BOOTSTRAP_CONTAINER_WRAPPER_CLASS_SYSTEM_PROPERTY,
                                          wrapperClassName,
                                          ZernovaContainerWrapper.class.getName()));
      }
      return (ZernovaContainerWrapper) wrapperClass.getConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(format("Unable to instantiate ZernovaContainerWrapper implementation '%s' from system property '%s'",
                                        wrapperClassName,
                                        MULE_BOOTSTRAP_CONTAINER_WRAPPER_CLASS_SYSTEM_PROPERTY),
                                 e);
    }
  }
}
