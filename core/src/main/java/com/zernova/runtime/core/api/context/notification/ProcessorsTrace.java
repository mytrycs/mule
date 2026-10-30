/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context.notification;

import com.zernova.api.annotation.NoImplement;

import java.io.Serializable;
import java.util.List;

/**
 * Keeps context information about the message processors that were executed as part of the processing of an event.
 *
 * @since 3.8.0
 *
 * @deprecated Use the message history functionality form the agent instead.
 */
@NoImplement
@Deprecated
public interface ProcessorsTrace extends Serializable {

  /**
   * @return an empty list.
   */
  List<String> getExecutedProcessors();

}
