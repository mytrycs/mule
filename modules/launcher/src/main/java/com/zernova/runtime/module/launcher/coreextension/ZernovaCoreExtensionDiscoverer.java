/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.launcher.coreextension;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.container.api.ZernovaCoreExtension;

import java.util.List;

/**
 * Defines a way to search for available {@link ZernovaCoreExtension}
 */
public interface ZernovaCoreExtensionDiscoverer {

  List<ZernovaCoreExtension> discover() throws ZernovaException;
}
