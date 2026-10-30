/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config.factory;

import com.zernova.runtime.core.internal.config.PropertyFactory;
import com.zernova.runtime.core.api.util.NetworkUtils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the local hostname from the local system
 */
public class HostNameFactory implements PropertyFactory {

  protected static final Logger logger = LoggerFactory.getLogger(HostNameFactory.class);

  public Object create(Map<?, ?> props) throws Exception {
    // we could use getCanonicalHostName here. however, on machines behind
    // NAT firewalls it seems that is often the NAT address, which corresponds
    // to an interface on the firewall, not on the local machine.
    try {
      return NetworkUtils.getLocalHost().getHostName();
    } catch (Exception e) {
      logger.warn("Unable to resolve hostname, defaulting to 'localhost': " + e.getMessage(), e);
      return "localhost";
    }
  }

}
