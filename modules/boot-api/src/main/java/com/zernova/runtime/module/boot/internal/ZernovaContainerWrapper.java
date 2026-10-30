/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.boot.internal;

import com.zernova.runtime.module.boot.api.ZernovaContainer;
import com.zernova.runtime.module.boot.api.ZernovaContainerLifecycleWrapper;

import org.apache.commons.cli.CommandLine;

/**
 * Manages the lifecycle of the {@link ZernovaContainer} including the execution of all the required {@link BootstrapConfigurer}s.
 * <p>
 * Implementations may interact with the underlying OS and handle signals/events.
 * <p>
 * Implementations must have a no-arguments constructor.
 *
 * @since 4.5
 */
public interface ZernovaContainerWrapper extends ZernovaContainerLifecycleWrapper {

  /**
   * Adds a {@link BootstrapConfigurer} to execute before starting.
   * <p>
   * The given {@link BootstrapConfigurer} will become managed by this wrapper instance, hence the disposal code will be executed
   * when this wrapper is disposed of. See {@link #dispose()}.
   *
   * @param bootstrapConfigurer The configurer to execute.
   */
  void addBootstrapConfigurer(BootstrapConfigurer bootstrapConfigurer);

  /**
   * Executes the registered {@link BootstrapConfigurer}, creates the {@link ZernovaContainer} using the given
   * {@code muleContainerFactory} and starts it.
   *
   * @param muleContainerFactory A {@link ZernovaContainerFactory} to use to create the {@link ZernovaContainer}.
   * @param commandLine          The command line parameters that were given to the bootstrapping application.
   */
  void configureAndStart(ZernovaContainerFactory muleContainerFactory, CommandLine commandLine);

  /**
   * Disposes any resources used by either the {@link BootstrapConfigurer}s or the {@link ZernovaContainerWrapper} implementation
   * itself.
   */
  void dispose();

  /**
   * Stops the bootstrapping sequence from progressing further. The bootstrapping application will eventually exit with the given
   * {@code exitCode} and the {@code message} should be available for troubleshooting.
   *
   * @param exitCode The exit code that the bootstrapping application should return to the OS.
   * @param message  A message to display for troubleshooting purposes.
   */
  void haltAndCatchFire(int exitCode, String message);

  /**
   * Requests that the JVM be restarted but then returns. This allows components to initiate a JVM exit and then continue,
   * allowing a normal shutdown initiated by the JVM via shutdown hooks.
   */
  @Override
  void restart();

  /**
   * Requests that the JVM be shutdown but then returns. This allows components to initiate a JVM exit and then continue, allowing
   * a normal shutdown initiated by the JVM via shutdown hooks.
   *
   * @param exitCode The exit code that the bootstrapping application should return to the OS.
   */
  @Override
  void stop(int exitCode);
}
