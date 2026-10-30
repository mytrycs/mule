/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation;

import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;

/**
 * Provides access to the actual {@link ConfigurationInstance} regardless of its decorator.
 * <p>
 * This is needed for functionality that needs the actual {@link ConfigurationInstance}, like transactions.
 *
 * @since 4.1.3
 */
public interface ExecutionContextConfigurationDecorator extends ConfigurationInstance {

  ConfigurationInstance getDecorated();
}
