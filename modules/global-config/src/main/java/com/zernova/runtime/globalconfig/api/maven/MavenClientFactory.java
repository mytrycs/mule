/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.globalconfig.api.maven;

import static com.zernova.maven.client.api.MavenClientProvider.discoverProvider;

import com.zernova.maven.client.api.MavenClient;
import com.zernova.maven.client.api.MavenClientProvider;
import com.zernova.maven.client.api.model.MavenConfiguration;
import com.zernova.runtime.api.util.LazyValue;

import java.util.function.Supplier;

/**
 * Allows to obtain {@link MavenClient} instances and configure how those are created.
 *
 * @since 4.5
 */
public class MavenClientFactory {

  public static Supplier<MavenClientProvider> mavenClientProvider =
      new LazyValue<>(() -> discoverProvider(MavenClientProvider.class.getClassLoader()));

  /**
   * Obtains a {@link MavenClient} though the {@link MavenClientProvider} configured through
   * {@link #setMavenClientProvider(Supplier)}.
   * <p>
   * By default, the {@link MavenClientProvider} is discovered through SPI.
   *
   * @param mavenConfiguration
   * @return a new {@link MavenClient} for the provided {@code mavenConfiguration}.
   */
  public static MavenClient createMavenClient(MavenConfiguration mavenConfiguration) {
    return mavenClientProvider.get().createMavenClient(mavenConfiguration);
  }

  /**
   * Registers the {@link MavenClientProvider} to use for obtaining a {@link MavenClient}.
   *
   * @param mavenClientProvider
   */
  public static void setMavenClientProvider(Supplier<MavenClientProvider> mavenClientProvider) {
    MavenClientFactory.mavenClientProvider = mavenClientProvider;
  }

}
