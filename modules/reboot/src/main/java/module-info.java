/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Bootstrap for the Mule CE Container.
 *
 * @moduleGraph
 * @since 4.5
 */
module com.zernova.boot {

  requires com.zernova.boot.api;

  requires org.apache.commons.cli;
}
