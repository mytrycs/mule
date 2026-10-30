/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.metadata.MetadataContext;

import java.util.Optional;

/**
 * A {@link MetadataContext} which is aware of the {@link ConnectionProvider} used to provide its connection.
 *
 * @since 4.4.0
 */
public interface ConnectionProviderAwareMetadataContext extends MetadataContext {

  /**
   * Retrieves the connection provider for the related a component and configuration
   *
   * @return Optional connection instance of {@link ConnectionProvider} type for the component.
   */
  Optional<ConnectionProvider> getConnectionProvider();

}
