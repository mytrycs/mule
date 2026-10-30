/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.management.stats;

import com.zernova.api.annotation.NoImplement;

/**
 * Allows for classifying a flow based on its name.
 *
 * @since 4.10
 */
@NoImplement
public interface FlowClassifier {

  enum FlowType {
    APIKIT, SOAPKIT, GENERIC
  }

  FlowType getFlowType(String flowName);
}
