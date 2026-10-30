/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.source;

import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Describes the operation modes that a {@link Source} can have when running in cluster mode.
 *
 * @since 1.1
 */
@MinZernovaVersion("4.1")
public enum SourceClusterSupport {

  /**
   * The source doesn't support cluster mode and hence only runs on the primary node.
   */
  NOT_SUPPORTED,

  /**
   * Runs on all the cluster nodes by default, but the user can choose to only run on the primary one
   */
  DEFAULT_ALL_NODES,

  /**
   * Only runs on the primary node by default, but the user can choose to run on all the nodes
   */
  DEFAULT_PRIMARY_NODE_ONLY
}
