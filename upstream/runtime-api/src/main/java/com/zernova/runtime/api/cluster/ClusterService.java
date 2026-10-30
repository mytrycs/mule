/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.cluster;

import com.zernova.api.annotation.NoImplement;

/**
 * Clustering service that provides information about the cluster state.
 */
@NoImplement
public interface ClusterService {

  /**
   * @return true if this is the cluster primary instance or if the runtime is not in cluster mode, false otherwise.
   */
  boolean isPrimaryPollingInstance();

}
