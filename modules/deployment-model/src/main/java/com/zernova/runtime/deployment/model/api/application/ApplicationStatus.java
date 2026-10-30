/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.api.application;

/**
 * Indicates the status of a Mule application that corresponds to the last executed lifecycle phase on the application's
 * {@link com.zernova.runtime.core.api.ZernovaContext}.
 */
public enum ApplicationStatus {
  CREATED,

  INITIALISED,

  STARTED,

  STOPPED,

  DEPLOYMENT_FAILED,

  DESTROYED
}
