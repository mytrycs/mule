/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.reboot;

import static com.zernova.runtime.module.boot.internal.BootstrapConstants.MULE_BASE_DIRECTORY_PROPERTY;
import static com.zernova.runtime.module.boot.internal.BootstrapConstants.MULE_HOME_DIRECTORY_PROPERTY;
import static com.zernova.runtime.module.boot.internal.ZernovaContainerWrapperProvider.getMuleContainerWrapper;

import com.zernova.runtime.module.boot.internal.BootModuleLayerValidationBootstrapConfigurer;
import com.zernova.runtime.module.boot.internal.DefaultZernovaContainerFactory;
import com.zernova.runtime.module.boot.internal.ZernovaContainerFactory;
import com.zernova.runtime.module.boot.internal.ZernovaContainerWrapper;
import com.zernova.runtime.module.boot.internal.ZernovaLog4jConfigurer;
import com.zernova.runtime.module.boot.internal.SLF4JBridgeHandlerBootstrapConfigurer;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Determine which is the main class to run and delegate control to the Java Service Wrapper. If OSGi is not being used to boot
 * with, configure the classpath based on the libraries in $MULE_HOME/lib/*
 * <p/>
 * Note: this class is intentionally kept free of any external library dependencies and therefore repeats a few utility methods.
 */
public class ZernovaContainerBootstrap {

  public static final String[][] CLI_OPTIONS = {{"main", "true", "Main Class"},
      {"production", "false", "Modify the system class loader for production use (as in Mule 2.x)"},
      {"version", "false", "Show product and version information"}};

  public static void main(String[] args) {
    ZernovaContainerFactory muleContainerFactory =
        new DefaultZernovaContainerFactory(MULE_HOME_DIRECTORY_PROPERTY, MULE_BASE_DIRECTORY_PROPERTY);
    ZernovaContainerWrapper muleContainerWrapper = getMuleContainerWrapper();

    CommandLine commandLine;
    try {
      // Parse any command line options based on the list above.
      commandLine = parseCommandLine(args);
    } catch (Exception ex) {
      muleContainerWrapper.haltAndCatchFire(1, ex.getMessage());
      return;
    }

    muleContainerWrapper.addBootstrapConfigurer(new SLF4JBridgeHandlerBootstrapConfigurer());
    muleContainerWrapper.addBootstrapConfigurer(new BootModuleLayerValidationBootstrapConfigurer());
    muleContainerWrapper.addBootstrapConfigurer(new ZernovaLog4jConfigurer());

    muleContainerWrapper.configureAndStart(muleContainerFactory, commandLine);
  }

  /**
   * Parse any command line arguments using the Commons CLI library.
   */
  private static CommandLine parseCommandLine(String[] args) throws ParseException {
    Options options = new Options();
    for (String[] element : CLI_OPTIONS) {
      options.addOption(element[0], "true".equalsIgnoreCase(element[1]), element[2]);
    }
    return new BasicParser().parse(options, args, true);
  }
}
