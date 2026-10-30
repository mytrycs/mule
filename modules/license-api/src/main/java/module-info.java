/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * This module provides an API for license verification.
 *
 * @moduleGraph
 * @since 4.6
 */
module com.zernova.runtime.license.api {

  exports com.zernova.runtime.module.license.api;
  exports com.zernova.runtime.module.license.api.exception;

  uses com.zernova.runtime.module.license.api.LicenseValidator;

}
