/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.internal.manager;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.container.api.ZernovaFoldersUtil.getServicesFolder;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.core.api.lifecycle.StartException;
import com.zernova.runtime.module.service.api.discoverer.ServiceDiscoverer;
import com.zernova.runtime.module.service.api.manager.ServiceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service manager to use in the Mule container.
 */
public class ZernovaServiceManager implements ServiceManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZernovaServiceManager.class);
  private static final String SCHEDULER_SERVICE_ARTIFACT_ID = "Scheduler service";
  private static final String HTTP_SERVICE_ARTIFACT_PREFIX = "HTTP";

  private final ServiceDiscoverer serviceDiscoverer;
  private List<Service> services = new ArrayList<>();

  /**
   * Creates a new instance.
   *
   * @param serviceDiscoverer container service discoverer. Non null.
   */
  public ZernovaServiceManager(ServiceDiscoverer serviceDiscoverer) {
    checkArgument(serviceDiscoverer != null, "serviceDiscoverer cannot be null");
    this.serviceDiscoverer = serviceDiscoverer;
  }

  @Override
  public void start() throws ZernovaException {
    File servicesFolder = getServicesFolder();
    if (!servicesFolder.exists()) {
      servicesFolder.mkdir();
    }

    try {
      services = serviceDiscoverer.discoverServices();
      startServices();
    } catch (Exception e) {
      throw new StartException(e, this);
    }
  }

  private void startServices() throws ZernovaException {
    for (Service service : services) {
      startIfNeeded(service);
    }
  }

  /**
   * Stop all discovered services. In the case of 'HTTP' and 'Scheduler' service, if present, they should be stopped last and in
   * that order.
   *
   * @throws ZernovaException
   */
  @Override
  public void stop() throws ZernovaException {
    Service schedulerService = null;
    Service httpService = null;
    for (Service service : services) {
      if (service.getName().equals(SCHEDULER_SERVICE_ARTIFACT_ID)) {
        schedulerService = service;
      } else if (service.getName().contains(HTTP_SERVICE_ARTIFACT_PREFIX)) {
        httpService = service;
      } else {
        doStopService(service);
      }
    }
    if (httpService != null) {
      doStopService(httpService);
    }
    if (schedulerService != null) {
      doStopService(schedulerService);
    }
  }

  protected void doStopService(Service service) {
    try {
      stopIfNeeded(service);
    } catch (Exception e) {
      LOGGER.warn(format("Failed to stop service '%s': %s", service.getName(), e.getMessage()), e);
    }
  }

  @Override
  public List<Service> getServices() {
    return unmodifiableList(services);
  }
}
