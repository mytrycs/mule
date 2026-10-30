/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.el;

import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;

import com.zernova.api.annotation.NoInstantiate;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;

/**
 * Expose information about the Mule instance:
 *
 * <li><b>clusterid</b> <i>Cluster ID</i>
 * <li><b>home</b> <i>Home directory</i>
 * <li><b>nodeid</b> <i>Cluster Node ID</i>
 * <li><b>version</b> <i>Mule Version</i>
 */
@NoInstantiate
public class ZernovaInstanceContext {

  private ZernovaContext muleContext;
  private ZernovaConfiguration configuration;

  public ZernovaInstanceContext(ZernovaContext muleContext, ZernovaConfiguration configuration) {
    this.muleContext = muleContext;
    this.configuration = configuration;
  }

  public String getVersion() {
    return getMuleManifest().getProductVersion();
  }

  public String getClusterId() {
    return muleContext.getClusterId();
  }

  public int getNodeId() {
    return muleContext.getClusterNodeId();
  }

  public String getHome() {
    return configuration.getMuleHomeDirectory();
  }

}
