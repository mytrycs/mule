/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.connectivity;

import com.zernova.runtime.api.connectivity.ConnectivityTestingService;
import com.zernova.runtime.module.deployment.impl.internal.application.DefaultApplicationFactory;
import com.zernova.runtime.module.tooling.api.connectivity.ConnectivityTestingServiceBuilder;
import com.zernova.runtime.module.tooling.internal.AbstractArtifactAgnosticServiceBuilder;
import com.zernova.runtime.module.tooling.internal.ApplicationSupplier;

/**
 * Default implementation for {@code ConnectivityTestingServiceBuilder}.
 *
 * @since 4.0
 */
public class DefaultConnectivityTestingServiceBuilder
    extends AbstractArtifactAgnosticServiceBuilder<ConnectivityTestingServiceBuilder, ConnectivityTestingService>
    implements ConnectivityTestingServiceBuilder {

  public DefaultConnectivityTestingServiceBuilder(DefaultApplicationFactory defaultApplicationFactory) {
    super(defaultApplicationFactory);
  }

  @Override
  protected ConnectivityTestingService createService(ApplicationSupplier applicationSupplier) {
    return new TemporaryArtifactConnectivityTestingService(applicationSupplier);
  }

}
