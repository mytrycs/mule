/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ocs;

import com.zernova.oauth.client.api.state.ResourceOwnerOAuthContext;
import com.zernova.runtime.extension.api.connectivity.oauth.ClientCredentialsState;
import com.zernova.runtime.oauth.api.PlatformManagedOAuthDancer;

import java.util.function.Consumer;

/**
 * Implementation of {@link AbstractPlatformOAuthStateAdapter} that implements {@link ClientCredentialsState}
 *
 * @since 4.3.0
 */
public class PlatformClientCredentialsOAuthStateAdapter extends AbstractPlatformOAuthStateAdapter
    implements ClientCredentialsState {

  public PlatformClientCredentialsOAuthStateAdapter(PlatformManagedOAuthDancer dancer,
                                                    Consumer<ResourceOwnerOAuthContext> onUpdate) {
    super(dancer, onUpdate);
  }
}
