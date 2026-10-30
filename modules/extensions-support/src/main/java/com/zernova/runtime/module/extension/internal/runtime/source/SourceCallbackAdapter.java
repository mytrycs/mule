/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.core.privileged.transaction.TransactionConfig;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.module.extension.internal.runtime.transaction.TransactionSourceBinder;
import com.zernova.sdk.api.runtime.source.Source;
import com.zernova.sdk.api.runtime.source.SourceCallback;

/**
 * Augments the {@link SourceCallback} contract with internal behavior not to be exposed on the public API
 *
 * @param <T> the generic type of the output values of the generated results
 * @param <A> the generic type of the attributes of the generated results
 * @since 4.0
 */
public interface SourceCallbackAdapter<T, A> extends SourceCallback<T, A> {

  /**
   * @return The {@link TransactionSourceBinder} to be used when binding a transaction
   */
  TransactionSourceBinder getTransactionSourceBinder();

  /**
   * @return The {@link ConfigurationInstance} of the source
   */
  ConfigurationInstance getConfigurationInstance();

  /**
   * @return The source {@link TransactionConfig}
   */
  TransactionConfig getTransactionConfig();

  /**
   * @return the {@link SourceConnectionManager} for the owning {@link Source}
   */
  SourceConnectionManager getSourceConnectionManager();

  /**
   * @return the name of the owning {@link Source}
   */
  String getOwningSourceName();

  /**
   * @return the name of the owning Extension
   */
  String getOwningExtensionName();

  /**
   * @return the source's {@link ComponentLocation}
   */
  ComponentLocation getSourceLocation();

  /**
   * @return the default transaction timeout
   */
  int getTimeout();
}
