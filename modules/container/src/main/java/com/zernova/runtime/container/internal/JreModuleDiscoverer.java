/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.internal;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static com.zernova.runtime.container.internal.JreExplorer.exploreJdk;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.container.api.ZernovaModule;
import com.zernova.runtime.container.api.discoverer.ModuleDiscoverer;
import com.zernova.runtime.jpms.api.ZernovaContainerModule;
import com.zernova.runtime.module.artifact.api.classloader.ExportedService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;

/**
 * Discovers the module corresponding to the JRE by creating a new {@link ZernovaModule} with the Java packages, resources and
 * services found on the used JRE.
 *
 * @since 4.0
 */
public class JreModuleDiscoverer implements ModuleDiscoverer {

  private static final Logger logger = getLogger(JreModuleDiscoverer.class);

  protected static final String JRE_MODULE_NAME = "jre";

  private static final Supplier<List<ZernovaContainerModule>> jreModulesSupplier = new LazyValue<>(() -> {
    Set<String> packages = new HashSet<>(1024);
    Set<String> resources = new HashSet<>(1024);
    List<ExportedService> services = new ArrayList<>(128);

    exploreJdk(packages, resources, services);

    if (logger.isDebugEnabled()) {
      logger.debug("Discovered JRE:\npackages: {}\nresources: {}\nservices: {}", packages, resources,
                   services.stream().map(p -> p.getServiceInterface() + ":" + p.getResource().toString()).collect(toList()));
    }

    ZernovaContainerModule jdkModule = new ZernovaModule(JRE_MODULE_NAME, packages, resources, emptySet(), emptySet(), services);

    return singletonList(jdkModule);
  });

  @Override
  public List<ZernovaContainerModule> discover() {
    return jreModulesSupplier.get();
  }
}
