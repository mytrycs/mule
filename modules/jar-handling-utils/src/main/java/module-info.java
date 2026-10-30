/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */

/**
 * This is a multi release module that allows handling of JDK internal JAR classes needed for Java 8 runtime and a cleaner way in 11+.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.jar.handling.utils {

  requires org.apache.commons.io;

  exports com.zernova.runtime.core.internal.util.jar to
      com.zernova.runtime.artifact.activation,
      com.zernova.runtime.deployment.model.impl;
}
