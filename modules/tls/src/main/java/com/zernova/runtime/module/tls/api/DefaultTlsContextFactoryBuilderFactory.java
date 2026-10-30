/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tls.api;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import static java.util.Collections.emptyMap;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.tls.AbstractTlsContextFactoryBuilderFactory;
import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.api.tls.TlsContextFactoryBuilder;
import com.zernova.runtime.module.tls.internal.DefaultTlsContextFactory;

/**
 * Default implementation of {@link AbstractTlsContextFactoryBuilderFactory} which has a default TLS context. This is injected
 * into each new {@link TlsContextFactoryBuilder} so that a single instance is exposed.
 *
 * @since 4.0
 */
public final class DefaultTlsContextFactoryBuilderFactory extends AbstractTlsContextFactoryBuilderFactory {

  private TlsContextFactory defaultTlsContextFactory = new DefaultTlsContextFactory(emptyMap());

  public DefaultTlsContextFactoryBuilderFactory() {
    try {
      initialiseIfNeeded(defaultTlsContextFactory);
    } catch (InitialisationException e) {
      throw new ZernovaRuntimeException(createStaticMessage("Failed to initialise default TlsContextFactory"), e);
    }
  }

  @Override
  protected TlsContextFactoryBuilder create() {
    return new com.zernova.runtime.module.tls.internal.DefaultTlsContextFactoryBuilder(defaultTlsContextFactory);
  }

}
