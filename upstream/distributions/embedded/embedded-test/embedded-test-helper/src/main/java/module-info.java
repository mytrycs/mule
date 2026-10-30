/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Mule Embedded Implementation Tests Helper.
 *
 * @moduleGraph
 * @since 4.8, 4.7.2, 4.6.6
 */
module com.zernova.distribution.embedded.test.helper {

  requires com.zernova.runtime.embedded.api;
  requires com.zernova.runtime.maven.client.test;

  requires junit;
  requires org.apache.commons.io;
  requires org.slf4j;
  requires zip4j;

  exports com.zernova.runtime.module.embedded.test.hepler;

}
